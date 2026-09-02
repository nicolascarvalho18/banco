package com.bancosap.repository;

import com.bancosap.entity.Account;
import com.bancosap.entity.PixKey;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PixKeyRepository extends JpaRepository<PixKey, Long> {
    List<PixKey> findByAccountId(Long accountId);
    Optional<PixKey> findByKeyValue(String keyValue);
    boolean existsByKeyValue(String keyValue);
    long countByAccount(Account account);
}
