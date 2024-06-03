package com.nasnav.service;

import com.nasnav.enumerations.ReferralTransactionsType;
import com.nasnav.enumerations.ReferralType;
import org.springframework.stereotype.Service;

@Service("influencerReferralWalletServiceImpl")
public class InfluencerReferralrWalletServiceImpl extends AbstractReferralWalletService implements ReferralWalletService{

    @Override
    protected ReferralType getReferralType() {
        return ReferralType.INFLUENCER;
    }

    @Override
    protected ReferralTransactionsType getInitialTransactionCreateType() {
        return ReferralTransactionsType.INFLUENCER_REGISTERATION;
    }
}
