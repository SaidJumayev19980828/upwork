package com.nasnav.service;

import com.nasnav.dao.LoyaltyBoosterRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.request.LoyaltyBoosterDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.LoyaltyBoosterUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class LoyaltyBoosterServiceImp implements LoyaltyBoosterService {

    @Autowired
    LoyaltyBoosterRepository loyaltyBoosterRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public LoyaltyBoosterUpdateResponse updateBooster(LoyaltyBoosterDTO dto) {
        validateBoosterDTO(dto);
        LoyaltyBoosterEntity entity = prepareBoosterEntity(dto);
        loyaltyBoosterRepository.save(entity);
        return new LoyaltyBoosterUpdateResponse(entity.getId());
    }

    @Override
    public List<LoyaltyBoosterDTO> getBoosterByOrgId(Long orgId) {
        if (orgId < 0) {
            orgId = securityService.getCurrentUserOrganizationId();
        }

        List<LoyaltyBoosterEntity> boosterEntities = loyaltyBoosterRepository.getAllByOrganization_IdAndIsActiveTrue(orgId);
        return prepareBoosterListToDto(boosterEntities);
    }

    private List<LoyaltyBoosterDTO> prepareBoosterListToDto(List<LoyaltyBoosterEntity> boosterEntities) {
        return boosterEntities.stream()
                .map(entity -> prepareBoosterEntityToDTO(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<LoyaltyBoosterDTO> getBoosters(Long orgId) {
        if (orgId > 0) {
            return getBoosterByOrgId(orgId);
        }
        return prepareBoosterListToDto(loyaltyBoosterRepository.findAll());
    }

    @Override
    public LoyaltyBoosterDTO getBoosterById(Long id) {
        Optional<LoyaltyBoosterEntity> boosterEntity = loyaltyBoosterRepository.findById(id);

        if(boosterEntity.isPresent()){
            return prepareBoosterEntityToDTO(boosterEntity.get());
        }

        throw new RuntimeBusinessException(NOT_FOUND, BOOSTER$PARAM$0002);
    }

    private LoyaltyBoosterDTO prepareBoosterEntityToDTO(LoyaltyBoosterEntity loyaltyBoosterEntity) {
        LoyaltyBoosterDTO dto = new LoyaltyBoosterDTO();

        dto.setId(loyaltyBoosterEntity.getId());
        dto.setLinkedFamilyMember(loyaltyBoosterEntity.getLinkedFamilyMember());
        dto.setBoosterName(loyaltyBoosterEntity.getBoosterName());
        dto.setIsActive(loyaltyBoosterEntity.getIsActive());
        dto.setPurchaseSize(loyaltyBoosterEntity.getPurchaseSize());
        dto.setReviewProducts(loyaltyBoosterEntity.getReviewProducts());
        dto.setNumberFamilyChildren(loyaltyBoosterEntity.getNumberFamilyChildren());
        dto.setNumberPurchaseOffline(loyaltyBoosterEntity.getNumberPurchaseOffline());
        dto.setOrgId(loyaltyBoosterEntity.getOrganization().getId());
        dto.setLevelBooster(dto.getLevelBooster());
        dto.setActivationMonths(loyaltyBoosterEntity.getActivationMonths());
        return dto;
    }

    @Override
    public void deleteBooster(Long boosterId) {
        loyaltyBoosterRepository.deleteById(boosterId);
    }

    @Override
    public void upgradeUserBooster(Long boosterId, Long userId) {
        if (boosterId == null || boosterId < 0) {
            return;
        }
        LoyaltyBoosterEntity loyaltyBoosterEntity = loyaltyBoosterRepository.findById(boosterId).get();
        UserEntity userEntity = userRepository.findById(userId).get();
        LoyaltyBoosterEntity userLoyaltyBoosterEntity = userEntity.getBooster();
        if (userLoyaltyBoosterEntity != null) {
            if (loyaltyBoosterEntity.getLevelBooster() > userLoyaltyBoosterEntity.getLevelBooster()) {
                userEntity.setBooster(loyaltyBoosterEntity);
                userRepository.save(userEntity);
            }
        }
    }

    private LoyaltyBoosterEntity prepareBoosterEntity(LoyaltyBoosterDTO dto) {
        LoyaltyBoosterEntity entity = loyaltyBoosterRepository.getByIdAndOrganization_Id(dto.getId(), dto.getOrgId())
                .orElseGet(LoyaltyBoosterEntity::new);
        if (dto.getOrgId() != null) {
            OrganizationEntity org = organizationRepository.findById(dto.getOrgId())
                    .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, S$0002, dto.getOrgId()));
            entity.setOrganization(org);
        } else {
            entity.setOrganization(securityService.getCurrentUserOrganization());
        }
        if (dto.getBoosterName() != null) {
            entity.setBoosterName(dto.getBoosterName());
        }
        if (dto.getLevelBooster() != null) {
            entity.setLevelBooster(dto.getLevelBooster());
        }
        if (dto.getActivationMonths() != null) {
            entity.setActivationMonths(dto.getActivationMonths());
        }
        if (dto.getLinkedFamilyMember() != null) {
            entity.setLinkedFamilyMember(dto.getLinkedFamilyMember());
        }
        if (dto.getNumberFamilyChildren() != null) {
            entity.setNumberFamilyChildren(dto.getNumberFamilyChildren());
        }
        if (dto.getNumberPurchaseOffline() != null) {
            entity.setNumberPurchaseOffline(dto.getNumberPurchaseOffline());
        }
        if (dto.getIsActive() != null) {
            entity.setIsActive(dto.getIsActive());
        } else {
            entity.setIsActive(true);
        }
        return entity;
    }

    private void validateBoosterDTO(LoyaltyBoosterDTO dto) {
        Long orgId = securityService.getCurrentUserOrganizationId();
		 if( dto != null && dto.getId() != null && dto.getId()  > 0){
            if (!loyaltyBoosterRepository.existsByIdAndOrganization_Id(dto.getId(), orgId)) {
                throw new RuntimeBusinessException(NOT_FOUND, BOOSTER$PARAM$0002, dto.getId());
            }
        }
    }
}
