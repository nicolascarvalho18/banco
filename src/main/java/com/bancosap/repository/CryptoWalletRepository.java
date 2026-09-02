package com.bancosap.repository;

import com.bancosap.entity.CryptoWallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CryptoWalletRepository extends JpaRepository<CryptoWallet, Long> {
    Optional<CryptoWallet> findByUserId(Long userId);
    Optional<CryptoWallet> findByWalletAddress(String walletAddress);
}
