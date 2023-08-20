package com.nasnav.persistence;

import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "employee_user_heart_beats_logs")
@EntityListeners(AuditingEntityListener.class)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUserHeartBeatsLogsEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne
    @JoinColumn(name = "employee_user_id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity employeeUserEntity;

    @Column(name = "created_at")
    @CreationTimestamp
    @CreatedDate
    private LocalDateTime creationDate;

}
