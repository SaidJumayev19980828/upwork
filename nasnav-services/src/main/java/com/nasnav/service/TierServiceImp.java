package com.nasnav.service;

import com.nasnav.dao.BoosterRepository;
import com.nasnav.dao.TierRepository;
import com.nasnav.dto.request.TierDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.TierEntity;
import com.nasnav.response.TierUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.PromotionStatus.INACTIVE;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class TierServiceImp implements TierService {

    @Autowired
    TierRepository tierRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    BoosterRepository boosterRepository;
    @Autowired
    UserService userService;

    @Override
    public TierUpdateResponse updateTier(TierDTO tiers) {
        validateTier(tiers);

        TierEntity entity = createTierEntity(tiers);
        entity = tierRepository.save(entity);
        return new TierUpdateResponse(entity.getId());
    }

    @Override
    public void deleteTier(Long id) {
        tierRepository.deleteById(id);
    }

    @Override
    public Optional<TierEntity> getTierById(Long id) {
        return tierRepository.findById(id);
    }

    @Override
    public List<TierEntity> getTiersBetweenAmountFromTo(Integer amountFrom, Integer amountTo) {
        return tierRepository.getByAmountFromAndTo(amountFrom, amountTo);
    }

    @Override
    public List<TierDTO> getTiers(Long orgId, Boolean isSpecial) {
        List<TierEntity> tierList;
        if (orgId > 0) {
            if (isSpecial) {
                tierList = tierRepository.getByOrganization_IdAndIsSpecial(orgId, isSpecial);
            } else {
                tierList = tierRepository.getByOrganization_Id(orgId);
            }
        } else {
            tierList = tierRepository.findAll();
        }
        return tierList.stream()
                .map(TierEntity::getRepresentation)
                .collect(toList());
    }

    @Override
    public TierEntity getTierByAmount(Integer amount) {
        return tierRepository.getByAmount(amount);
    }

    @Override
    public void addNewTierToUser(Long userId, Long tierId) {
        userService.updateUserByTierId(tierId, userId);
    }

    private TierEntity createTierEntity(TierDTO tiers) {
        TierEntity entity = getOrCreateTierEntity(tiers);
        if (isUpdateOperation(tiers) && !isInactiveTier(entity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , TIERS$PARAM$0002, tiers.getId());
        }
        OrganizationEntity organization = securityService.getCurrentUserOrganization();
        entity.setOrganization(organization);
        entity.setIsActive(tiers.getIsActive());
        entity.setIsSpecial(tiers.getIsSpecial());
        entity.setNoOfPurchaseFrom(tiers.getNoOfPurchaseFrom());
        entity.setNoOfPurchaseTo(tiers.getNoOfPurchaseTo());
        entity.setSellingPrice(tiers.getSellingPrice());
        entity.setTierName(tiers.getTierName());
        if (tiers != null && tiers.getIsSpecial()) {
            entity.setBooster(null);
        } else {
            entity.setBooster(boosterRepository.findById(tiers.getBoosterId()).get());
        }
        return entity;
    }

    private TierEntity getOrCreateTierEntity(TierDTO tiers) {
        return ofNullable(tiers)
                .map(TierDTO::getId)
                .map(this::getExistingTier)
                .orElseGet(TierEntity::new);
    }

    private TierEntity getExistingTier(Long id) {
        return ofNullable(id)
                .flatMap(tierRepository::findById)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
                        , TIERS$PARAM$0001, id));
    }

    private boolean isUpdateOperation(TierDTO tiers) {
        return nonNull(tiers.getId());
    }

    private boolean isInactiveTier(TierEntity tiers) {
        return Objects.equals(INACTIVE.getValue(), tiers.getIsActive());
    }

    private void validateTier(TierDTO tiers) {
        if (anyIsNull(tiers, tiers.getTierName())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , TIERS$PARAM$0003, tiers.toString());
        }
    }
}
