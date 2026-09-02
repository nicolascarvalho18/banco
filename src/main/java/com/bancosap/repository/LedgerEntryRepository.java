package com.bancosap.repository;

import com.bancosap.entity.LedgerEntry;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LedgerEntryRepository extends JpaRepository<LedgerEntry, Long> {
    Optional<LedgerEntry> findByEntryCode(String entryCode);
    List<LedgerEntry> findByTransactionReferenceOrderByCreatedAtAsc(String transactionReference);
    Page<LedgerEntry> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Page<LedgerEntry> findByUserIdAndAssetSymbolOrderByCreatedAtDesc(Long userId, String assetSymbol, Pageable pageable);
}
