package com.nasnav.service.impl;

import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;

import com.nasnav.service.AbstractReferralWalletService;
import com.nasnav.service.ReferralWalletService;
import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

@Service("referralWalletServiceImpl")
@Primary
@RequiredArgsConstructor
public class ReferralWalletServiceImplementation extends AbstractReferralWalletService implements ReferralWalletService {

    @Override
    protected ReferralType getReferralType() {
        return ReferralType.USER;
    }

    @Override
    protected ReferralTransactionsType getInitialTransactionCreateType() {
        return ReferralTransactionsType.ACCEPT_REFERRAL_CODE;
    }
}
