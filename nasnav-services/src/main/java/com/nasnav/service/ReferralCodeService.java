package com.nasnav.service;

import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeCreateResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.exceptions.RuntimeBusinessException;

public interface ReferralCodeService {

    ReferralCodeDto get(Long id);
    ReferralCodeDto get(String referralCode);

    PaginatedResponse getList(int pageNo, int pageSize);
    ReferralCodeCreateResponse create(ReferralCodeDto referralCodeDto) throws RuntimeBusinessException;

    void update(ReferralCodeDto referralCodeDto);

    void activate(String referralCode);
    void deActivate(String referralCode);

    void delete(String referralCode);



}
