package com.bancosap.service;

import com.bancosap.dto.response.TransactionResponse;
import com.bancosap.entity.Account;
import com.bancosap.entity.Transaction;
import com.bancosap.enums.TransactionCategory;
import com.bancosap.enums.TransactionType;
import com.bancosap.exception.ResourceNotFoundException;
import com.bancosap.repository.AccountRepository;
import com.bancosap.repository.TransactionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class StatementService {

    private final TransactionRepository transactionRepository;
    private final AccountRepository accountRepository;

    public StatementService(TransactionRepository transactionRepository, AccountRepository accountRepository) {
        this.transactionRepository = transactionRepository;
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    public Page<TransactionResponse> getFilteredStatement(
            Long userId,
            LocalDateTime startDate,
            LocalDateTime endDate,
            TransactionCategory category,
            TransactionType type,
            BigDecimal minAmount,
            BigDecimal maxAmount,
            Pageable pageable) {

        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        return transactionRepository.findFilteredTransactions(
                account.getId(),
                startDate,
                endDate,
                category,
                type,
                minAmount,
                maxAmount,
                pageable
        ).map(tx -> mapToResponse(tx, account.getId()));
    }

    @Transactional(readOnly = true)
    public TransactionResponse getReceiptByAuthCode(String authCode) {
        Transaction tx = transactionRepository.findByAuthenticationCode(authCode)
                .orElseThrow(() -> new ResourceNotFoundException("Comprovante não localizado com o código informado."));
        return mapToResponse(tx, null);
    }

    @Transactional(readOnly = true)
    public byte[] exportStatementCsv(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        Account account = accountRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Conta não encontrada."));

        Page<Transaction> page = transactionRepository.findFilteredTransactions(
                account.getId(), startDate, endDate, null, null, null, null, Pageable.unpaged());

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(out, true, StandardCharsets.UTF_8);

        // Header CSV formatado em padrão brasileiro (separador ponto e vírgula)
        writer.println("Data/Hora;Código Autenticação;Tipo;Categoria;Descrição;Origem;Destino;Valor (R$);Status");

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        for (Transaction tx : page.getContent()) {
            boolean isIncoming = tx.getDestinationAccount() != null && tx.getDestinationAccount().getId().equals(account.getId());
            String formattedAmount = (isIncoming ? "+" : "-") + tx.getAmount().toString().replace(".", ",");
            String source = tx.getSourceAccount() != null && tx.getSourceAccount().getUser() != null ?
                    tx.getSourceAccount().getUser().getFullName() : "Banco SAP";
            String dest = tx.getDestinationName() != null ? tx.getDestinationName() : "Titular";

            writer.printf("%s;%s;%s;%s;\"%s\";\"%s\";\"%s\";%s;%s%n",
                    tx.getCreatedAt().format(dtf),
                    tx.getAuthenticationCode(),
                    tx.getTransactionType().name(),
                    tx.getCategory().name(),
                    tx.getDescription() != null ? tx.getDescription().replace("\"", "'") : "",
                    source,
                    dest,
                    formattedAmount,
                    tx.getStatus().name()
            );
        }

        writer.flush();
        return out.toByteArray();
    }

    private TransactionResponse mapToResponse(Transaction tx, Long myAccountId) {
        TransactionResponse r = new TransactionResponse();
        r.setId(tx.getId());
        r.setAuthenticationCode(tx.getAuthenticationCode());
        r.setType(tx.getTransactionType());
        r.setTypeDescription(tx.getTransactionType().name());
        r.setAmount(tx.getAmount());
        r.setFee(tx.getFee());
        r.setCategory(tx.getCategory());
        r.setDescription(tx.getDescription());
        r.setDestinationName(tx.getDestinationName());
        r.setDestinationDocument(tx.getDestinationDocument());
        r.setDestinationBank(tx.getDestinationBank());
        if (tx.getSourceAccount() != null && tx.getSourceAccount().getUser() != null) {
            r.setSourceName(tx.getSourceAccount().getUser().getFullName());
            r.setSourceAccountNumber(tx.getSourceAccount().getAccountNumber());
        }
        r.setStatus(tx.getStatus());
        r.setCreatedAt(tx.getCreatedAt());

        boolean isIncoming = false;
        if (myAccountId != null && tx.getDestinationAccount() != null) {
            isIncoming = tx.getDestinationAccount().getId().equals(myAccountId);
        } else if (tx.getTransactionType() == TransactionType.DEPOSITO || tx.getTransactionType() == TransactionType.PIX_RECEBIDO || tx.getTransactionType() == TransactionType.TRANSFERENCIA_RECEBIDA) {
            isIncoming = true;
        }
        r.setIncoming(isIncoming);

        return r;
    }
}
