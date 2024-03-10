package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;

import java.time.LocalDateTime;

import static javax.persistence.FetchType.LAZY;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "referral_settings")
public class ReferralSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String name;

    @OneToOne(fetch = LAZY)
    @JoinColumn(name = "org_id")
    private OrganizationEntity organization;

    @Column(name = "constraints")
    private String constraints;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
