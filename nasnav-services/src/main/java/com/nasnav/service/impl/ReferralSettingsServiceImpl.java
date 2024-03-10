package com.nasnav.service.impl;

import com.nasnav.dao.ReferralSettingsRepo;
import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.enumerations.ReferralCodeType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.ReferralSettingsMapper;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ReferralSettings;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ReferralSettingsService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

import static com.nasnav.enumerations.ReferralCodeType.ORDER_DISCOUNT_PERCENTAGE;
import static com.nasnav.exceptions.ErrorCodes.REF$PARAM$0009;
import static com.nasnav.exceptions.ErrorCodes.REF$PARAM$0010;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class ReferralSettingsServiceImpl implements ReferralSettingsService {

    private final ReferralSettingsMapper referralSettingsMapper;

    private final ReferralSettingsRepo referralSettingsRepo;

    @Autowired
    private OrganizationService organizationService;

    @Autowired
    private SecurityService securityService;

    @Override
    public ReferralSettingsDto create(ReferralSettingsDto referralSettingsDto) {
        ReferralSettings referralSettings = referralSettingsMapper.map(referralSettingsDto);

        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        OrganizationEntity organization = organizationService.getOrganizationById(currentOrganizationId);

        referralSettings.setOrganization(organization);
        referralSettingsRepo.save(referralSettings);

        return ReferralSettingsDto.builder()
                .id(referralSettings.getId())
                .build();
    }

    @Override
    public Map<ReferralCodeType, BigDecimal> getValue(ReferralCodeType referralCodeType){
        Long currentOrganizationId= securityService.getCurrentUserOrganizationId();

        ReferralSettings referralSettings =
                referralSettingsRepo.findByOrganization_Id(currentOrganizationId)
                        .orElseThrow(() ->  new RuntimeBusinessException(NOT_FOUND, REF$PARAM$0010));


        return Map.of(ORDER_DISCOUNT_PERCENTAGE,
                referralSettingsMapper.map(referralSettings).getConstraints().get(ORDER_DISCOUNT_PERCENTAGE).getValue().multiply(new BigDecimal("100")).stripTrailingZeros());
    }

}
