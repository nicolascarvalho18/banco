package com.bancosap.repository;

import com.bancosap.entity.CryptoAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CryptoAssetRepository extends JpaRepository<CryptoAsset, Long> {
    List<CryptoAsset> findByWalletId(Long walletId);
    Optional<CryptoAsset> findByWalletIdAndSymbol(Long walletId, String symbol);
}
