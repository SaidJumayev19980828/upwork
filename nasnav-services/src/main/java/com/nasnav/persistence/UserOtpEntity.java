package com.nasnav.persistence;

import com.nasnav.service.OTP.OTPType;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "user_otp")
@Data
@NoArgsConstructor
public class UserOtpEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;

    private String otp;

    @Enumerated(EnumType.STRING)
    private OTPType type;

    @Column(name = "created_at")
    private Date createdAt;
}
