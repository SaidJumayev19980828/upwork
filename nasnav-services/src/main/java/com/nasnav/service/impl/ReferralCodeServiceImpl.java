package com.nasnav.service.impl;

import com.nasnav.dao.ReferralCodeRepo;
import com.nasnav.dto.PaginatedResponse;
import com.nasnav.dto.referral_code.ReferralCodeCreateResponse;
import com.nasnav.dto.referral_code.ReferralCodeDto;
import com.nasnav.enumerations.ReferralCodeStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.mappers.ReferralCodeMapper;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ReferralCodeEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Random;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
@RequiredArgsConstructor
public class ReferralCodeServiceImpl implements ReferralCodeService {

    private final ReferralCodeMapper referralCodeMapper;

    private final ReferralCodeRepo referralCodeRepo;

    private final OrganizationService organizationService;

    private final UserService userService;

    private final SecurityService securityService;

    private final EmployeeUserService employeeUserService;


    @Override
    public ReferralCodeDto get(Long id) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();

        return referralCodeMapper.map(
                referralCodeRepo.findByIdAndOrganization_Id(id, currentOrganizationId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0002, id))
        );
    }

    @Override
    public ReferralCodeDto get(String referralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
       return referralCodeMapper.map(referralCodeRepo.findByReferralCodeAndOrganization_Id(referralCode, currentOrganizationId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode))
               );

    }

    public PaginatedResponse<ReferralCodeDto> getList(int pageNo, int pageSize){
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        Pageable pageable = PageRequest.of(pageNo, pageSize, Sort.by("createdAt").descending());

        return referralCodeMapper.map(
                referralCodeRepo.findAllByOrganization_id(currentOrganizationId, pageable)
        );
    }


    @Override
    public ReferralCodeCreateResponse create(ReferralCodeDto referralCodeDto) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();

        UserEntity userEntity = userService.getByEmailAndOrganizationId(referralCodeDto.getReferrerEmail(), currentOrganizationId);
        if(userEntity == null){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, U$LOG$0010, referralCodeDto.getReferrerEmail());
        }

        OrganizationEntity currentOrganizationEntity = new OrganizationEntity();
        currentOrganizationEntity.setId(organizationService.getOrganizationById(currentOrganizationId, 0).getId());

        ReferralCodeEntity newReferralCodeEntity = referralCodeMapper.map(referralCodeDto);
        newReferralCodeEntity.setReferralCode(generate(6));
        newReferralCodeEntity.setOrganization(currentOrganizationEntity);
        newReferralCodeEntity.setUser(userEntity);
        newReferralCodeEntity.setCreatedBy(securityService.getCurrentUser().getId());

        referralCodeRepo.save(newReferralCodeEntity);

        return ReferralCodeCreateResponse.builder()
                .id(newReferralCodeEntity.getId())
                .referralCode(newReferralCodeEntity.getReferralCode())
                .build();
    }

    @Override
    public void update(ReferralCodeDto referralCodeDto) {
        ReferralCodeEntity existReferralCodeEntity = referralCodeRepo.findById(referralCodeDto.getId())
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0002, referralCodeDto.getId()));
      referralCodeRepo.save(referralCodeMapper.map(referralCodeDto, existReferralCodeEntity));
    }

    @Override
    public void activate(String referralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        ReferralCodeEntity existReferralCode =  referralCodeRepo.findByReferralCodeAndOrganization_Id(referralCode, currentOrganizationId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));

        existReferralCode.setStatus(ReferralCodeStatus.ACTIVE.getValue());

        referralCodeRepo.save(existReferralCode);
    }

    @Override
    public void deActivate(String referralCode) {
        Long currentOrganizationId = securityService.getCurrentUserOrganizationId();
        ReferralCodeEntity existReferralCode =  referralCodeRepo.findByReferralCodeAndOrganization_Id(referralCode, currentOrganizationId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, REF$PARAM$0003, referralCode));

        existReferralCode.setStatus(ReferralCodeStatus.IN_ACTIVE.getValue());

        referralCodeRepo.save(existReferralCode);
    }

    @Override
    public void delete(String referralCode) {

    }

    private String generate(int codeLength){
        char[] chars = "abcdefghijklmnopqrstuvwxyz1234567890".toCharArray();
        StringBuilder sb = new StringBuilder();
        Random random = new SecureRandom();
        for (int i = 0; i < codeLength; i++) {
            char c = chars[random.nextInt(chars.length)];
            sb.append(c);
        }
        String output = sb.toString();
        System.out.println(output);
        return output ;
    }

}
