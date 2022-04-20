package com.nasnav.service;

import com.nasnav.dao.LoyaltyBoosterRepository;
import com.nasnav.dao.LoyaltyTierRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.LoyaltyTierEntity;
import com.nasnav.persistence.UserEntity;
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
    private LoyaltyTierRepository tierRepository;
    @Autowired
    private LoyaltyBoosterRepository loyaltyBoosterRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UserRepository userRepository;
    @Override
    public LoyaltyTierUpdateResponse updateTier(LoyaltyTierDTO tier) {
        validateTier(tier);

        LoyaltyTierEntity entity = createTierEntity(tier);
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
            tierList = isSpecial? tierRepository.getByOrganization_IdAndIsSpecial(orgId, isSpecial): tierRepository.getByOrganization_Id(orgId);
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
    public UserRepresentationObject changeUserTier(Long userId, Long tierId, Long orgId) {
        LoyaltyTierEntity tier = getExistingTier(tierId);
        UserEntity user = userRepository.findByIdAndOrganizationId(userId, orgId).orElseThrow(() ->new RuntimeBusinessException(NOT_ACCEPTABLE
                , U$0001, userId));

        user.setTier(tier);
        userRepository.save(user);
        return user.getRepresentation();

    }

    private LoyaltyTierEntity createTierEntity(LoyaltyTierDTO tier) {
        LoyaltyTierEntity entity = getOrCreateTierEntity(tier);
        if (isUpdateOperation(tier) && !isInactiveTier(entity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , TIERS$PARAM$0002, tier.getId());
        }
        OrganizationEntity organization = organizationRepository.findOneById(tier.getOrgId());
        entity.setOrganization(organization);
        entity.setIsActive(tier.getIsActive());
        entity.setIsSpecial(tier.getIsSpecial());
        entity.setNoOfPurchaseFrom(tier.getNoOfPurchaseFrom());
        entity.setNoOfPurchaseTo(tier.getNoOfPurchaseTo());
        entity.setSellingPrice(tier.getSellingPrice());
        entity.setTierName(tier.getTierName());
        entity.setCashBackPercentage(tier.getCashBackPercentage());
        entity.setCoefficient(tier.getCoefficient());

        if (tier.getIsSpecial()) {
            entity.setBooster(null);
        } else if(tier.getBoosterId() != null && tier.getBoosterId() > 0){
            entity.setBooster(loyaltyBoosterRepository.findById(tier.getBoosterId()).get());
        } else {
            entity.setBooster(null);
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

    private boolean isUpdateOperation(LoyaltyTierDTO tier) {
        return nonNull(tier.getId()) && tier.getId() > 0;
    }

    private boolean isInactiveTier(LoyaltyTierEntity tier) {
        return Objects.equals(INACTIVE.getValue(), tier.getIsActive());
    }

    private void validateTier(LoyaltyTierDTO tiers) {
        if (anyIsNull(tiers, tiers.getTierName(), tiers.getOrgId(), tiers.getCoefficient())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , TIERS$PARAM$0003, tiers.toString());
        }
    }
}
