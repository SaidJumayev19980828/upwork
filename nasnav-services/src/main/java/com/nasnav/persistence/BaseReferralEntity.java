package com.nasnav.persistence;

import com.nasnav.enumerations.ReferralType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.Column;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;
import java.time.LocalDateTime;

@MappedSuperclass
@Getter @Setter
@AllArgsConstructor
@NoArgsConstructor
public class BaseReferralEntity {

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "referral_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ReferralType referralType;

    @Column(name = "created_at", nullable = false, columnDefinition = "TIMESTAMP DEFAULT CURRENT_TIMESTAMP")
    @CreationTimestamp
    private LocalDateTime createdAt;

}
