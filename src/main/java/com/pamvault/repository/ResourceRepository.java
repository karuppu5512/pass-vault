package com.pamvault.repository;

import com.pamvault.entity.Resource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ResourceRepository extends JpaRepository<Resource, Long> {

    List<Resource> findByOwnerIdOrderByNameAsc(Long ownerId);

    @Query("select s.resource from ResourceShare s where s.sharedWith.id = :userId order by s.resource.name")
    List<Resource> findSharedWith(@Param("userId") Long userId);
}
