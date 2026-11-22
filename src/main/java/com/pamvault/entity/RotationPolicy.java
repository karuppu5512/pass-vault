package com.pamvault.entity;

import com.pamvault.enums.RotationUnit;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "rotation_policies")
@Getter
@Setter
@NoArgsConstructor
public class RotationPolicy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", unique = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    private boolean enabled = true;

    private int intervalValue;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private RotationUnit intervalUnit;

    private int passwordLength;

    private boolean includeSpecialChars;

    private LocalDateTime nextRotationAt;

    private LocalDateTime lastRunAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "updated_by_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User updatedBy;

    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void touch() {
        updatedAt = LocalDateTime.now();
    }
}
