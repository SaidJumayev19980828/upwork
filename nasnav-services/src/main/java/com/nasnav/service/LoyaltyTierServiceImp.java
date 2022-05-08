package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
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
    private LoyaltyPointConfigRepository configRepo;
    @Autowired
    private LoyaltyBoosterRepository loyaltyBoosterRepository;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityService securityService;

    @Override
    public LoyaltyTierUpdateResponse updateTier(LoyaltyTierDTO tier) {
        validateTier(tier);

        LoyaltyTierEntity entity = createTierEntity(tier);
        return new LoyaltyTierUpdateResponse(entity.getId());
    }

    @Override
    public void deleteTier(Long id) {
        LoyaltyTierEntity tier = getExistingTier(id);

        configRepo.findByDefaultTier_IdAndIsActive(tier.getId(), true)
                .ifPresent(c -> {throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0022, c.getId());});

        List<UserEntity> usersWithTier = userRepository.findByTier_Id(tier.getId());
        if (!usersWithTier.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0023, usersWithTier.size());
        }

        tierRepository.delete(tier);
    }

    @Override
    public LoyaltyTierDTO getTierById(Long id) {
        return getExistingTier(id).getRepresentation();
    }

    @Override
    public List<LoyaltyTierEntity> getTiersBetweenAmountFromTo(Integer amountFrom, Integer amountTo) {
        return tierRepository.getByAmountFromAndTo(amountFrom, amountTo);
    }

    @Override
    public List<LoyaltyTierDTO> getTiers(Long orgId, Boolean isSpecial) {
        List<LoyaltyTierEntity> tierList;
        if (orgId != null) {
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
    public UserRepresentationObject changeUserTier(Long userId, Long tierId) {
        Long orgId = securityService.getCurrentUserOrganizationId();

        LoyaltyTierEntity tier = getExistingTier(tierId);

        UserEntity user = userRepository.findByIdAndOrganizationId(userId, orgId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, U$0001, userId));

        user.setTier(tier);
        userRepository.save(user);
        return user.getRepresentation();

    }

    private LoyaltyTierEntity createTierEntity(LoyaltyTierDTO tier) {
        LoyaltyTierEntity entity = getOrCreateTierEntity(tier);
        if (isUpdateOperation(tier) && !isInactiveTier(entity)) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, TIERS$PARAM$0002, tier.getId());
        }
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
        } else if(tier.getBoosterId() != null){
            LoyaltyBoosterEntity booster = loyaltyBoosterRepository.findById(tier.getBoosterId())
                            .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, BOOSTER$PARAM$0002, tier.getBoosterId()));
            entity.setBooster(booster);
        }
        return tierRepository.save(entity);
    }

    private LoyaltyTierEntity getOrCreateTierEntity(LoyaltyTierDTO tiers) {
        return ofNullable(tiers)
                .map(LoyaltyTierDTO::getId)
                .map(this::getExistingTier)
                .orElseGet(this::createNewTierEntity);
    }

    private LoyaltyTierEntity createNewTierEntity() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        LoyaltyTierEntity entity = new LoyaltyTierEntity();
        OrganizationEntity organization = organizationRepository.findOneById(orgId);
        entity.setOrganization(organization);
        return entity;
    }

    private LoyaltyTierEntity getExistingTier(Long id) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return ofNullable(id)
                .flatMap(tierId -> tierRepository.findByIdAndOrganization_Id(tierId, orgId))
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0019, id));
    }

    private boolean isUpdateOperation(LoyaltyTierDTO tier) {
        return nonNull(tier.getId()) && tier.getId() > 0;
    }

    private boolean isInactiveTier(LoyaltyTierEntity tier) {
        return Objects.equals(INACTIVE.getValue(), tier.getIsActive());
    }

    private void validateTier(LoyaltyTierDTO tier) {
        if (anyIsNull(tier, tier.getTierName(), tier.getOrgId(), tier.getCoefficient())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , TIERS$PARAM$0003, tier.toString());
        }
    }
}
