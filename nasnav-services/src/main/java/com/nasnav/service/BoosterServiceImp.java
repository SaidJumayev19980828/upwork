package com.nasnav.service;

import com.nasnav.dao.BoosterRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.request.BoosterDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.response.LoyaltyBoosterUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
public class BoosterServiceImp implements BoosterService {

    @Autowired
    BoosterRepository boosterRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    OrganizationRepository organizationRepository;
    @Autowired
    UserRepository userRepository;

    @Override
    public LoyaltyBoosterUpdateResponse updateBooster(BoosterDTO dto) {
        validateBoosterDTO(dto);
        BoosterEntity entity = prepareBoosterEntity(dto);
        boosterRepository.save(entity);
        return new LoyaltyBoosterUpdateResponse(entity.getId());
    }

    @Override
    public List<BoosterDTO> getBoosterByOrgId(Long orgId) {
        if (orgId < 0) {
            orgId = securityService.getCurrentUserOrganizationId();
        }

        List<BoosterEntity> boosterEntities = boosterRepository.getAllByOrganization_IdAndIsActiveTrue(orgId);
        return prepareBoosterListToDto(boosterEntities);
    }

    private List<BoosterDTO> prepareBoosterListToDto(List<BoosterEntity> boosterEntities) {
        return boosterEntities.stream()
                .map(entity -> prepareBoosterEntityToDTO(entity))
                .collect(Collectors.toList());
    }

    @Override
    public List<BoosterDTO> getBoosters() {
        return prepareBoosterListToDto(boosterRepository.findAll());
    }

    @Override
    public BoosterDTO getBoosterById(Long id) {
        Optional<BoosterEntity> boosterEntity = boosterRepository.findById(id);

        if(boosterEntity.isPresent()){
            return prepareBoosterEntityToDTO(boosterEntity.get());
        }

        throw new RuntimeBusinessException(NOT_FOUND, BOOSTER$PARAM$0002);
    }

    private BoosterDTO prepareBoosterEntityToDTO(BoosterEntity boosterEntity) {
        BoosterDTO dto = new BoosterDTO();

        dto.setId(boosterEntity.getId());
        dto.setLinkedFamilyMember(boosterEntity.getLinkedFamilyMember());
        dto.setBoosterName(boosterEntity.getBoosterName());
        dto.setIsActive(boosterEntity.getIsActive());
        dto.setPurchaseSize(boosterEntity.getPurchaseSize());
        dto.setReviewProducts(boosterEntity.getReviewProducts());
        dto.setNumberFamilyChildren(boosterEntity.getNumberFamilyChildren());
        dto.setNumberPurchaseOffline(boosterEntity.getNumberPurchaseOffline());
        dto.setOrgId(boosterEntity.getOrganization().getId());
        dto.setLevelBooster(dto.getLevelBooster());
        dto.setActivationMonths(boosterEntity.getActivationMonths());
        return dto;
    }

    @Override
    public void deleteBooster(Long boosterId) {
        boosterRepository.deleteById(boosterId);
    }

    @Override
    public void upgradeUserBooster(Long boosterId, Long userId) {
        if (boosterId == null || boosterId < 0) {
            return;
        }
        BoosterEntity boosterEntity = boosterRepository.findById(boosterId).get();
        UserEntity userEntity = userRepository.findById(userId).get();
        BoosterEntity userBoosterEntity = userEntity.getBooster();
        if (userBoosterEntity != null) {
            if (boosterEntity.getLevelBooster() > userBoosterEntity.getLevelBooster()) {
                userEntity.setBooster(boosterEntity);
                userRepository.save(userEntity);
            }
        }
    }

    private BoosterEntity prepareBoosterEntity(BoosterDTO dto) {
        BoosterEntity entity = boosterRepository.getByIdAndOrganization_Id(dto.getId(), dto.getOrgId())
                .orElseGet(BoosterEntity::new);
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

    private void validateBoosterDTO(BoosterDTO dto) {
        Long orgId = securityService.getCurrentUserOrganizationId();
		 if(dto.getId()  > 0){
            if (!boosterRepository.existsByIdAndOrganization_Id(dto.getId(), orgId)) {
                throw new RuntimeBusinessException(NOT_FOUND, BOOSTER$PARAM$0002, dto.getId());
            }
        }
    }
}
