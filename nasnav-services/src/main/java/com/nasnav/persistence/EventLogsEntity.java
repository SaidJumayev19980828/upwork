package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Optional;

@Table(name = "event_logs")
@Entity
@Data
public class EventLogsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "interested_at")
    private LocalDateTime interestedAt;

    @Column(name = "attend_at")
    private LocalDateTime attendAt;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "event_id", nullable = false)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EventEntity event;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne(fetch = FetchType.EAGER, optional = true)
    @JoinColumn(name = "employee_id", nullable = true)
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity employee;

    public boolean isInfluencer() {
        return Optional.ofNullable(getUser())
                .map(UserEntity::getInfluencer)
                .map(InfluencerEntity::getApproved)
                .orElseGet(() -> Optional.ofNullable(getEmployee())
                        .map(EmployeeUserEntity::getInfluencer)
                        .map(InfluencerEntity::getApproved)
                        .orElse(false));
    }


}
