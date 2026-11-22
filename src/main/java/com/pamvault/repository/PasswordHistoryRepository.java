package com.pamvault.repository;

import com.pamvault.entity.PasswordHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PasswordHistoryRepository extends JpaRepository<PasswordHistory, Long> {

    List<PasswordHistory> findByAccountIdOrderByChangedAtDesc(Long accountId);
}
