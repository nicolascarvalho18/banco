package com.bancosap.repository;

import com.bancosap.entity.SupportTicket;
import com.bancosap.enums.TicketStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SupportTicketRepository extends JpaRepository<SupportTicket, Long> {
    Page<SupportTicket> findByUserIdOrderByUpdatedAtDesc(Long userId, Pageable pageable);
    Optional<SupportTicket> findByProtocol(String protocol);
    Page<SupportTicket> findByStatus(TicketStatus status, Pageable pageable);
}
