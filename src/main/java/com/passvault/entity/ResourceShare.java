package com.passvault.entity;

import com.passvault.enums.SharePermission;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "resource_shares",
        uniqueConstraints = @UniqueConstraint(columnNames = {"resource_id", "shared_with_id"}))
@Getter
@Setter
@NoArgsConstructor
public class ResourceShare {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "resource_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Resource resource;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "shared_with_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User sharedWith;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private SharePermission permission;

    private LocalDateTime sharedAt;

    @PrePersist
    void onCreate() {
        sharedAt = LocalDateTime.now();
    }
}
