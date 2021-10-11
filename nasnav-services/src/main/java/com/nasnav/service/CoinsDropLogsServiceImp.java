package com.nasnav.service;

import com.nasnav.dao.CoinsDropLogsRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.CoinsDropEntity;
import com.nasnav.persistence.CoinsDropLogsEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.COINS$PARAM$0003;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class CoinsDropLogsServiceImp implements CoinsDropLogsService {

    @Autowired
    CoinsDropLogsRepository coinsLogsRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    UserRepository userRepository;

    @Override
    public Optional<CoinsDropLogsEntity> getByOrganizationId(Long orgId) {
        return coinsLogsRepository.getByOrganization_Id(orgId);
    }

    @Override
    public CoinsDropLogsEntity getByOrganizationIdAndCoinsDropId(Long orgId, Long coinsDropId) {
        return coinsLogsRepository.getByOrganization_IdAndCoinsDrop_Id(orgId, coinsDropId);
    }

    @Override
    public CoinsDropLogsEntity getByOrganizationIdAndUserId(Long orgId, Long userId) {
        return coinsLogsRepository.getByOrganization_IdAndUser_Id(orgId, userId);
    }

    @Override
    public CoinsDropLogsEntity getByOrganizationIdAndCoinsDropIdAndUserId(Long orgId, Long coinsDropId, Long userId) {
        return coinsLogsRepository.getByOrganization_IdAndCoinsDrop_IdAndUser_Id(orgId, coinsDropId, userId);
    }

    @Override
    public Long updateCoinsDropLog(CoinsDropEntity coins) {
        validateCoinsDropLog(coins);

        CoinsDropLogsEntity entity = createCoinsDropLogEntity(coins);
        return coinsLogsRepository.save(entity).getId();
    }

    private CoinsDropLogsEntity createCoinsDropLogEntity(CoinsDropEntity dropEntity) {
        OrganizationEntity organization = securityService.getCurrentUserOrganization();
        Long userId = securityService.getCurrentUser().getId();
        UserEntity user = userRepository.findById(userId).get();
        CoinsDropLogsEntity entity = new CoinsDropLogsEntity();
        entity.setOrganization(organization);
        entity.setCoinsDrop(dropEntity);
        entity.setUser(user);
        entity.setIsActive(true);
        return entity;
    }

    private void validateCoinsDropLog(CoinsDropEntity coinsDropEntity) {
        if (anyIsNull(coinsDropEntity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , COINS$PARAM$0003, coinsDropEntity.toString());
        }
    }

}
