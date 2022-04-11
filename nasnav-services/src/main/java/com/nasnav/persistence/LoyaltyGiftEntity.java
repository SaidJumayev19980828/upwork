package com.nasnav.persistence;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.GiftDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.beans.BeanUtils;

import javax.persistence.*;
import java.math.BigDecimal;

@Data
@Entity
@Table(name = "loyalty_gift")
@EqualsAndHashCode(callSuper=false)
public class LoyaltyGiftEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_from_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private UserEntity userFrom;

    @ManyToOne
    @JoinColumn(name = "user_to_id")
    @JsonIgnore
    @EqualsAndHashCode.Exclude
    @lombok.ToString.Exclude
    private UserEntity userTo;

    @Column(name = "points")
    private BigDecimal points;

    @Column(name = "email")
    private String email;

    @Column(name = "phone_number")
    private String phoneNumber;

    @Column(name = "is_active")
    private Boolean isActive;

    @Column(name = "is_redeem")
    private Boolean isRedeem;

    public GiftDTO getRepresentation() {
        GiftDTO dto = new GiftDTO();
        BeanUtils.copyProperties(this, dto);
        dto.setUserToEmail(userTo.getEmail());
        return dto;
    }
}
