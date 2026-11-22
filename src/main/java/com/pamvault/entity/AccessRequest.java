package com.pamvault.entity;

import com.pamvault.enums.RequestStatus;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;

@Entity
@Table(name = "access_requests")
@Getter
@Setter
@NoArgsConstructor
public class AccessRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Account account;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "requester_id")
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User requester;

    @Column(nullable = false, length = 500)
    private String reason;

    private int durationMinutes;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private RequestStatus status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewed_by_id")
    @OnDelete(action = OnDeleteAction.SET_NULL)
    private User reviewedBy;

    @Column(length = 500)
    private String reviewComment;

    private LocalDateTime requestedAt;
    private LocalDateTime reviewedAt;
    private LocalDateTime accessStartTime;
    private LocalDateTime accessEndTime;
    private LocalDateTime closedAt;

    @PrePersist
    void onCreate() {
        requestedAt = LocalDateTime.now();
    }
}
