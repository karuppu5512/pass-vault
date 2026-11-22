package com.pamvault.repository;

import com.pamvault.entity.RotationPolicy;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface RotationPolicyRepository extends JpaRepository<RotationPolicy, Long> {

    Optional<RotationPolicy> findByAccountId(Long accountId);

    List<RotationPolicy> findByEnabledTrueAndNextRotationAtLessThanEqual(LocalDateTime time);
}
