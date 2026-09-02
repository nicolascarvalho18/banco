package com.bancosap.repository;

import com.bancosap.entity.SimulatedOrder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SimulatedOrderRepository extends JpaRepository<SimulatedOrder, Long> {
    Optional<SimulatedOrder> findByAuthenticationCode(String authenticationCode);
    Page<SimulatedOrder> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);
    Optional<SimulatedOrder> findByIdempotencyKeyAndUserId(String idempotencyKey, Long userId);
}
