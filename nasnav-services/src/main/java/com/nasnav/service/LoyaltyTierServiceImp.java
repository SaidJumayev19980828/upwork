package com.nasnav.service;

import com.nasnav.dao.LoyaltyBoosterRepository;
import com.nasnav.dao.LoyaltyTierRepository;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.LoyaltyTierEntity;
import com.nasnav.response.LoyaltyTierUpdateResponse;
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
public class LoyaltyTierServiceImp implements LoyaltyTierService {

    @Autowired
    LoyaltyTierRepository tierRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    LoyaltyBoosterRepository loyaltyBoosterRepository;
    @Autowired
    UserService userService;

    @Override
    public LoyaltyTierUpdateResponse updateTier(LoyaltyTierDTO tiers) {
        validateTier(tiers);

        LoyaltyTierEntity entity = createTierEntity(tiers);
        entity = tierRepository.save(entity);
        return new LoyaltyTierUpdateResponse(entity.getId());
    }

    @Override
    public void deleteTier(Long id) {
        tierRepository.deleteById(id);
    }

    @Override
    public Optional<LoyaltyTierEntity> getTierById(Long id) {
        return tierRepository.findById(id);
    }

    @Override
    public List<LoyaltyTierEntity> getTiersBetweenAmountFromTo(Integer amountFrom, Integer amountTo) {
        return tierRepository.getByAmountFromAndTo(amountFrom, amountTo);
    }

    @Override
    public List<LoyaltyTierDTO> getTiers(Long orgId, Boolean isSpecial) {
        List<LoyaltyTierEntity> tierList;
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
                .map(LoyaltyTierEntity::getRepresentation)
                .collect(toList());
    }

    @Override
    public LoyaltyTierEntity getTierByAmount(Integer amount) {
        return tierRepository.getByAmount(amount);
    }

    @Override
    public void addNewTierToUser(Long userId, Long tierId) {
        userService.updateUserByTierId(tierId, userId);
    }

    private LoyaltyTierEntity createTierEntity(LoyaltyTierDTO tiers) {
        LoyaltyTierEntity entity = getOrCreateTierEntity(tiers);
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
            entity.setBooster(loyaltyBoosterRepository.findById(tiers.getBoosterId()).get());
        }
        return entity;
    }

    private LoyaltyTierEntity getOrCreateTierEntity(LoyaltyTierDTO tiers) {
        return ofNullable(tiers)
                .map(LoyaltyTierDTO::getId)
                .map(this::getExistingTier)
                .orElseGet(LoyaltyTierEntity::new);
    }

    private LoyaltyTierEntity getExistingTier(Long id) {
        return ofNullable(id)
                .flatMap(tierRepository::findById)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
                        , TIERS$PARAM$0001, id));
    }

    private boolean isUpdateOperation(LoyaltyTierDTO tiers) {
        return nonNull(tiers.getId());
    }

    private boolean isInactiveTier(LoyaltyTierEntity tiers) {
        return Objects.equals(INACTIVE.getValue(), tiers.getIsActive());
    }

    private void validateTier(LoyaltyTierDTO tiers) {
        if (anyIsNull(tiers, tiers.getTierName())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , TIERS$PARAM$0003, tiers.toString());
        }
    }
}
