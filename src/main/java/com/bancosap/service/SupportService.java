package com.bancosap.service;

import com.bancosap.dto.request.SupportReplyRequest;
import com.bancosap.dto.request.SupportTicketRequest;
import com.bancosap.dto.response.SupportMessageResponse;
import com.bancosap.dto.response.SupportTicketResponse;
import com.bancosap.entity.SupportMessage;
import com.bancosap.entity.SupportTicket;
import com.bancosap.entity.User;
import com.bancosap.enums.TicketSenderType;
import com.bancosap.enums.TicketStatus;
import com.bancosap.exception.ResourceNotFoundException;
import com.bancosap.repository.SupportMessageRepository;
import com.bancosap.repository.SupportTicketRepository;
import com.bancosap.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SupportService {

    private final SupportTicketRepository ticketRepository;
    private final SupportMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final SecureRandom secureRandom = new SecureRandom();

    public SupportService(SupportTicketRepository ticketRepository, SupportMessageRepository messageRepository,
                          UserRepository userRepository) {
        this.ticketRepository = ticketRepository;
        this.messageRepository = messageRepository;
        this.userRepository = userRepository;
    }

    public List<Map<String, Object>> getFaqList() {
        return List.of(
                Map.of(
                        "category", "PIX e Transferências",
                        "questions", List.of(
                                Map.of("q", "Qual o prazo de compensação do PIX?", "a", "O PIX é instantâneo e concluído em até 10 segundos, 24 horas por dia, todos os dias da semana."),
                                Map.of("q", "Existe limite diário para envio de PIX?", "a", "Sim. Por padrão o limite diário é de R$ 10.000,00 e o noturno (das 20h às 06h) é de R$ 1.000,00. Você pode solicitar ajustes no menu de limites."),
                                Map.of("q", "Posso cancelar um PIX após o envio?", "a", "Como a liquidação é instantânea, uma vez enviado o valor é creditado imediatamente na conta recebedora. Para reaver o valor, utilize o contato do destinatário.")
                        )
                ),
                Map.of(
                        "category", "Cartões Virtuais e Físicos",
                        "questions", List.of(
                                Map.of("q", "Como funciona o Cartão Virtual temporário?", "a", "O cartão virtual temporário é gerado para compras únicas na internet. Após a utilização ou expiração, os dados não podem ser reutilizados, garantindo máxima segurança."),
                                Map.of("q", "Como ajustar o limite de um cartão?", "a", "Acesse a aba 'Cartões', selecione o cartão desejado e utilize o controle deslizante para definir o limite desejado em tempo real.")
                        )
                ),
                Map.of(
                        "category", "Criptoativos Demonstrativos",
                        "questions", List.of(
                                Map.of("q", "Os valores de criptomoedas são reais?", "a", "Não. A carteira de criptoativos do Banco SAP opera estritamente em ambiente demonstrativo e pedagógico, utilizando cotações de simulação."),
                                Map.of("q", "Como transferir cripto para outro usuário?", "a", "Acesse o módulo de Criptoativos, clique em 'Transferir P2P', insira o endereço de carteira SAP do destinatário, selecione a moeda e a quantidade.")
                        )
                ),
                Map.of(
                        "category", "Segurança e Acesso",
                        "questions", List.of(
                                Map.of("q", "O que é o PIN de segurança de 4 a 6 dígitos?", "a", "O PIN é uma assinatura digital utilizada para autorizar transações financeiras, emissão de cartões e compras, sem a necessidade de expor sua senha de acesso."),
                                Map.of("q", "Minha conta foi bloqueada por tentativas incorretas. O que fazer?", "a", "Após 5 tentativas com senha inválida, a conta é suspensa temporariamente por 15 minutos por proteção. Aguarde o prazo ou utilize a opção 'Esqueci minha senha'.")
                        )
                )
        );
    }

    public String getChatbotAnswer(String userMessage) {
        String msg = userMessage.toLowerCase().trim();

        if (msg.contains("pix") || msg.contains("chave") || msg.contains("qr")) {
            return "O PIX no Banco SAP é instantâneo e gratuito! Você pode cadastrar até 5 chaves (CPF, E-mail, Telefone ou Chave Aleatória), gerar QR Codes 'Copia e Cola' e emitir comprovantes oficiais.";
        }
        if (msg.contains("cartao") || msg.contains("cartão") || msg.contains("limite") || msg.contains("cvv")) {
            return "No menu de Cartões, você pode criar cartões virtuais temporários ou recorrentes, bloquear/desbloquear com 1 clique e ajustar o limite de gastos instantaneamente com segurança.";
        }
        if (msg.contains("cripto") || msg.contains("bitcoin") || msg.contains("eth") || msg.contains("solana")) {
            return "Nossa carteira de Criptoativos permite simular a compra, venda e transferência P2P de Bitcoin, Ethereum, Solana, USDT e Cardano com cotações em tempo real. Lembre-se: é um ambiente 100% demonstrativo!";
        }
        if (msg.contains("boleto") || msg.contains("pagar") || msg.contains("codigo de barras")) {
            return "Para pagar boletos, vá em 'Pagamentos & Boletos', cole ou digite a linha digitável e confirme com seu PIN. Você também pode gerar um boleto simulado para carregar seu saldo de teste.";
        }
        if (msg.contains("senha") || msg.contains("pin") || msg.contains("bloqueio") || msg.contains("segurança")) {
            return "Sua segurança é nossa prioridade. Para alterar sua senha ou configurar seu PIN de 4 dígitos, acesse seu Perfil. Em caso de bloqueio por tentativas incorretas, a liberação ocorre automaticamente em 15 minutos.";
        }
        if (msg.contains("saldo") || msg.contains("extrato") || msg.contains("comprovante")) {
            return "Você pode consultar seu extrato detalhado com filtros por data, categoria e valor na aba 'Extrato', além de exportar relatórios em CSV e visualizar o comprovante oficial de cada operação.";
        }
        if (msg.contains("ola") || msg.contains("olá") || msg.contains("bom dia") || msg.contains("boa tarde") || msg.contains("boa noite")) {
            return "Olá! Sou o Assistente Virtual do Banco SAP. Como posso te ajudar hoje? Você pode tirar dúvidas sobre PIX, Cartões, Boletos, Extrato, Criptoativos ou Segurança.";
        }

        return "Compreendi sua dúvida! Caso precise de auxílio detalhado, você pode abrir um ticket de suporte na aba 'Novo Chamado' que nossa equipe de especialistas responderá rapidamente.";
    }

    @Transactional
    public SupportTicketResponse createTicket(Long userId, SupportTicketRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        String protocol = "SAP-" + (LocalDate.now().getYear()) + "-" + (100000 + secureRandom.nextInt(900000));
        SupportTicket ticket = new SupportTicket(user, protocol, request.getSubject(), request.getCategory());
        ticket = ticketRepository.save(ticket);

        SupportMessage message = new SupportMessage(ticket, TicketSenderType.USER, user.getFullName(), request.getMessage());
        messageRepository.save(message);

        // Resposta automática do bot de triagem
        SupportMessage botMessage = new SupportMessage(
                ticket,
                TicketSenderType.BOT,
                "Assistente Virtual SAP",
                "Recebemos o seu chamado! O protocolo é " + protocol + ". Um de nossos especialistas analisará sua solicitação em breve."
        );
        messageRepository.save(botMessage);

        return mapToTicketResponse(ticket);
    }

    @Transactional(readOnly = true)
    public Page<SupportTicketResponse> getUserTickets(Long userId, Pageable pageable) {
        return ticketRepository.findByUserIdOrderByUpdatedAtDesc(userId, pageable)
                .map(this::mapToTicketResponse);
    }

    @Transactional(readOnly = true)
    public SupportTicketResponse getTicketByProtocol(Long userId, String protocol) {
        SupportTicket ticket = ticketRepository.findByProtocol(protocol)
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado."));
        return mapToTicketResponse(ticket);
    }

    @Transactional
    public SupportMessageResponse replyTicket(Long userId, SupportReplyRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado."));

        SupportTicket ticket = ticketRepository.findById(request.getTicketId())
                .filter(t -> t.getUser().getId().equals(userId))
                .orElseThrow(() -> new ResourceNotFoundException("Chamado não encontrado."));

        SupportMessage message = new SupportMessage(ticket, TicketSenderType.USER, user.getFullName(), request.getMessage());
        message = messageRepository.save(message);

        ticket.setUpdatedAt(LocalDateTime.now());
        ticketRepository.save(ticket);

        return new SupportMessageResponse(message.getId(), message.getSenderType(), message.getSenderName(), message.getMessage(), message.getCreatedAt());
    }

    private SupportTicketResponse mapToTicketResponse(SupportTicket t) {
        List<SupportMessageResponse> msgResponses = messageRepository.findByTicketIdOrderByCreatedAtAsc(t.getId())
                .stream()
                .map(m -> new SupportMessageResponse(m.getId(), m.getSenderType(), m.getSenderName(), m.getMessage(), m.getCreatedAt()))
                .collect(Collectors.toList());

        return new SupportTicketResponse(
                t.getId(),
                t.getProtocol(),
                t.getSubject(),
                t.getCategory(),
                t.getStatus(),
                t.getUser().getEmail(),
                t.getUser().getFullName(),
                t.getCreatedAt(),
                t.getUpdatedAt(),
                msgResponses
        );
    }
}
