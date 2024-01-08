package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.*;
import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.LoyaltyTierDTO;
import com.nasnav.enumerations.LoyaltyPointType;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.LoyaltyTierUpdateResponse;
import com.nasnav.service.LoyaltyTierService;
import com.nasnav.service.SecurityService;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class LoyaltyTierServiceImp implements LoyaltyTierService {

    private static final Logger logger = LogManager.getLogger("LoyaltyTierService");

    @Autowired
    private LoyaltyTierRepository tierRepository;
    @Autowired
    private LoyaltyPointConfigRepository configRepo;
    @Autowired
    private OrganizationRepository organizationRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private SecurityService securityService;
    @Autowired
    private ObjectMapper objectMapper;
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
        LoyaltyTierEntity e = getExistingTier(id);
        return getTierRepresentation(e);
    }

    public HashMap<LoyaltyPointType, BigDecimal> readTierJsonStr(String jsonStr){
        try {
            return objectMapper.readValue(jsonStr, new TypeReference<HashMap<LoyaltyPointType, BigDecimal>>() {});
        } catch (Exception e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, G$JSON$0001, jsonStr);
        }
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
                .map(this::getTierRepresentation)
                .collect(toList());
    }

    private LoyaltyTierDTO getTierRepresentation(LoyaltyTierEntity entity) {
        LoyaltyTierDTO dto = entity.getRepresentation();
        dto.setConstraints(readTierJsonStr(entity.getConstraints()));
        return dto;
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
        }
        if (tier.getConstraints() != null) {
            entity.setConstraints(serializeDTO(tier.getConstraints()));
        }
        return tierRepository.save(entity);
    }

    private String serializeDTO(Map<LoyaltyPointType, BigDecimal> dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (JsonProcessingException e) {
            logger.error(e,e);
            return "{}";
        }
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
            if (anyIsNull(tier, tier.getTierName(), tier.getConstraints())) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, TIERS$PARAM$0003, tier.toString());
            }
    }
}
