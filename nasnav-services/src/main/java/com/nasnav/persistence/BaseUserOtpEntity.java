package com.nasnav.persistence;

import com.nasnav.service.otp.OtpType;
import lombok.Data;
import lombok.EqualsAndHashCode;

import javax.persistence.*;
import java.util.Date;

@MappedSuperclass
@Data
@EqualsAndHashCode(callSuper=true)
public abstract class BaseUserOtpEntity<U extends DefaultBusinessEntity<Long>> extends DefaultBusinessEntity<Long> {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private U user;

    private String otp;

    @Enumerated(EnumType.STRING)
    private OtpType type;

    private Long attempts = 0L;

    @Column(name = "created_at")
    private Date createdAt;

    public Long incrementAttempts() {
        return ++attempts;
    }
}
