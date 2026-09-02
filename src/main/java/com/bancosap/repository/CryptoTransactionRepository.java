package com.bancosap.repository;

import com.bancosap.entity.CryptoTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoTransactionRepository extends JpaRepository<CryptoTransaction, Long> {
    Optional<CryptoTransaction> findByTxHash(String txHash);

    @Query("SELECT ct FROM CryptoTransaction ct WHERE ct.sourceWallet.id = :walletId OR ct.destinationWallet.id = :walletId ORDER BY ct.createdAt DESC")
    Page<CryptoTransaction> findAllByWalletId(@Param("walletId") Long walletId, Pageable pageable);
}
