package com.bancosap.repository;

import com.bancosap.entity.VirtualCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VirtualCardRepository extends JpaRepository<VirtualCard, Long> {
    List<VirtualCard> findByAccountId(Long accountId);
    Optional<VirtualCard> findByIdAndAccountId(Long id, Long accountId);
    Optional<VirtualCard> findByCardNumberToken(String cardNumberToken);
}
