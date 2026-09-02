package com.bancosap.repository;

import com.bancosap.entity.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByUserId(Long userId);
    Optional<Account> findByAccountNumber(String accountNumber);
    Optional<Account> findByAgencyNumberAndAccountNumber(String agencyNumber, String accountNumber);
    boolean existsByAccountNumber(String accountNumber);

    @Query("SELECT a FROM Account a JOIN a.user u WHERE u.email = :email")
    Optional<Account> findByUserEmail(@Param("email") String email);

    @Query("SELECT a FROM Account a JOIN a.user u WHERE u.cpf = :cpf")
    Optional<Account> findByUserCpf(@Param("cpf") String cpf);
}
