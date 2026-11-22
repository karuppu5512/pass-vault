package com.pamvault.repository;

import com.pamvault.entity.ResourceShare;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ResourceShareRepository extends JpaRepository<ResourceShare, Long> {

    Optional<ResourceShare> findByResourceIdAndSharedWithId(Long resourceId, Long userId);

    List<ResourceShare> findByResourceId(Long resourceId);
}
