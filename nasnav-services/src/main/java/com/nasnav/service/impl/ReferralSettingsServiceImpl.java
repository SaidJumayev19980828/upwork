package com.nasnav.service.impl;

import com.nasnav.dao.ReferralSettingsRepo;
import com.nasnav.dto.referral_code.ReferralSettingsDto;
import com.nasnav.mappers.ReferralSettingsMapper;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ReferralSettings;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ReferralSettingsService;
import com.nasnav.service.SecurityService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
}
