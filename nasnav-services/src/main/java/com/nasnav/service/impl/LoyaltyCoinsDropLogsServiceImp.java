package com.nasnav.service.impl;

import com.nasnav.dao.LoyaltyCoinsDropLogsRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.LoyaltyCoinsDropEntity;
import com.nasnav.persistence.LoyaltyCoinsDropLogsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.LoyaltyCoinsDropLogsService;
import com.nasnav.service.SecurityService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.COINS$PARAM$0003;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class LoyaltyCoinsDropLogsServiceImp implements LoyaltyCoinsDropLogsService {

    @Autowired
    LoyaltyCoinsDropLogsRepository coinsLogsRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    UserRepository userRepository;

    @Override
    public Optional<LoyaltyCoinsDropLogsEntity> getByOrganizationId(Long orgId) {
        return coinsLogsRepository.getByOrganization_Id(orgId);
    }

    @Override
    public LoyaltyCoinsDropLogsEntity getByOrganizationIdAndCoinsDropId(Long orgId, Long coinsDropId) {
        return coinsLogsRepository.getByOrganization_IdAndCoinsDrop_Id(orgId, coinsDropId);
    }

    @Override
    public LoyaltyCoinsDropLogsEntity getByOrganizationIdAndUserId(Long orgId, Long userId) {
        return coinsLogsRepository.getByOrganization_IdAndUser_Id(orgId, userId);
    }

    @Override
    public LoyaltyCoinsDropLogsEntity getByOrganizationIdAndCoinsDropIdAndUserId(Long orgId, Long coinsDropId, Long userId) {
        return coinsLogsRepository.getByOrganization_IdAndCoinsDrop_IdAndUser_Id(orgId, coinsDropId, userId);
    }

    @Override
    public Long updateCoinsDropLog(LoyaltyCoinsDropEntity coins) {
        validateCoinsDropLog(coins);

        LoyaltyCoinsDropLogsEntity entity = createCoinsDropLogEntity(coins);
        return coinsLogsRepository.save(entity).getId();
    }

    private LoyaltyCoinsDropLogsEntity createCoinsDropLogEntity(LoyaltyCoinsDropEntity dropEntity) {
        OrganizationEntity organization = securityService.getCurrentUserOrganization();
        Long userId = securityService.getCurrentUser().getId();
        UserEntity user = userRepository.findById(userId).get();
        LoyaltyCoinsDropLogsEntity entity = new LoyaltyCoinsDropLogsEntity();
        entity.setOrganization(organization);
        entity.setCoinsDrop(dropEntity);
        entity.setUser(user);
        entity.setIsActive(true);
        return entity;
    }

    private void validateCoinsDropLog(LoyaltyCoinsDropEntity loyaltyCoinsDropEntity) {
        if (anyIsNull(loyaltyCoinsDropEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , COINS$PARAM$0003, "coinsDropEntity");
        }
    }

}
