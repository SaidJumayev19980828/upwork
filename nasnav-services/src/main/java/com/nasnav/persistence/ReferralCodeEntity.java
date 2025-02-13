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
@Table(name = "referral_codes")
@EqualsAndHashCode(callSuper=false)
public class ReferralCodeEntity  extends BaseReferralEntity{

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "referral_code")
    private String referralCode;

    @Column(name = "parent_referral_code")
    private String parentReferralCode;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "org_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private OrganizationEntity organization;

    @Column(name = "status")
    private Integer status;

    @ManyToOne
    @JoinColumn(name = "settings_id")
    private ReferralSettings settings;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "accept_token")
    private String acceptReferralToken;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
