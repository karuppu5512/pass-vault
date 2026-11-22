package com.passvault.entity;

import com.passvault.enums.ChangeSource;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "password_history")
@Getter
@Setter
@NoArgsConstructor
public class PasswordHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @Column(nullable = false, length = 1024)
    private String encryptedPassword;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ChangeSource source;

    private String changedBy;

    private String reason;

    private LocalDateTime changedAt;

    @PrePersist
    void onCreate() {
        changedAt = LocalDateTime.now();
    }
}
