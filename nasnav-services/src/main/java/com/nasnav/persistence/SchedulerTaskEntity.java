package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@NoArgsConstructor
@Table(name = "scheduler_tasks")
public class SchedulerTaskEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

    @Column(name = "starts_at")
    private LocalDateTime startsAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity user;

    @ManyToOne
    @JoinColumn(name = "employee_user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity employeeUser;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "availability_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private AvailabilityEntity availability;

    private String type;

    public SchedulerTaskEntity(AvailabilityEntity entity) {
        this.createdAt = LocalDateTime.now();
        this.startsAt = entity.getStartsAt();
        this.user = entity.getUser();
        this.employeeUser = entity.getEmployeeUser();
        this.availability = entity;
        this.type = "appointment";
    }

}
