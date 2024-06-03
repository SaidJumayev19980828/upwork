package com.nasnav.persistence;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "influencer_referral")
public class InfluencerReferral {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "first_name")
    private String firstName;

    @Column(name = "last_name")
    private String lastName;

    @Column(name = "user_name")
    private String userName;

    @Column(name = "password")
    private String password;

    @OneToOne
    @JoinColumn(name = "referral_code_id", referencedColumnName = "id")
    private ReferralCodeEntity referral;

    @OneToOne
    @JoinColumn(name = "referral_wallet_id", referencedColumnName = "id")
    private ReferralWallet referralWallet;

    @OneToOne
    @JoinColumn(name = "referral_settings_id", referencedColumnName = "id")
    private ReferralSettings referralSettings;

}
