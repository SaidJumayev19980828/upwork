package com.nasnav.service;

import com.nasnav.dao.*;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.LoyaltyTierUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
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
        Long orgId = securityService.getCurrentUserOrganizationId();

        configRepo.findByDefaultTier_IdAndIsActive(tier.getId(), true)
                .ifPresent(c -> {throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0022, c.getId());});

        List<UserEntity> usersWithTier = userRepository.findByTier_Id(tier.getId());
        if (!usersWithTier.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$LOY$0023, usersWithTier.size());
        }

        configRepo.deleteSoftDeletedConfigs(orgId, tier.getId());
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
    public List<LoyaltyTierDTO> getTiers(Boolean isSpecial) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        List<LoyaltyTierEntity> tierList =
                isSpecial != null ?
                tierRepository.getByOrganization_IdAndIsSpecial(orgId, isSpecial)
                :
                tierRepository.getByOrganization_Id(orgId);

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
        if (tier.getIsActive() != null) {
            entity.setIsActive(tier.getIsActive());
        }
        if (tier.getIsSpecial() != null) {
            entity.setIsSpecial(tier.getIsSpecial());
        }
        if (tier.getNoOfPurchaseFrom() != null) {
            entity.setNoOfPurchaseFrom(tier.getNoOfPurchaseFrom());
        }
        if (tier.getNoOfPurchaseTo() != null) {
            entity.setNoOfPurchaseTo(tier.getNoOfPurchaseTo());
        }
        if (tier.getTierName() != null) {
            entity.setTierName(tier.getTierName());
        }
        if (tier.getCashBackPercentage() != null) {
            entity.setCashBackPercentage(tier.getCashBackPercentage());
        }if (tier.getCoefficient() != null) {
            entity.setCoefficient(tier.getCoefficient());
        }

        if (tier.getIsSpecial() != null &&
                tier.getIsSpecial()) {
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
        return tier.getOperation().equals("update");
    }

    private boolean isInactiveTier(LoyaltyTierEntity tier) {
        return Objects.equals(false, tier.getIsActive());
    }

    private void validateTier(LoyaltyTierDTO tier) {
        if (tier.getOperation() == null || !List.of("create", "update").contains(tier.getOperation())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$PRO$0007);
        }
        if (tier.getOperation().equals("create"))
            if (anyIsNull(tier, tier.getTierName(), tier.getCoefficient())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, TIERS$PARAM$0003, tier.toString());
            }
    }
}
