package com.bancosap.repository;

import com.bancosap.entity.InternalTransfer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InternalTransferRepository extends JpaRepository<InternalTransfer, Long> {
    Optional<InternalTransfer> findByAuthenticationCode(String authenticationCode);

    @Query("SELECT t FROM InternalTransfer t WHERE t.sender.id = :userId OR t.recipient.id = :userId ORDER BY t.createdAt DESC")
    Page<InternalTransfer> findByUserId(@Param("userId") Long userId, Pageable pageable);
}
