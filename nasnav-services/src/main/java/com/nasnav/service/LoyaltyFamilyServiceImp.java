package com.nasnav.service;

import com.nasnav.dao.LoyaltyFamilyRepository;
import com.nasnav.dto.request.LoyaltyFamilyDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.LoyaltyFamilyEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.LoyaltyFamilyUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class LoyaltyFamilyServiceImp implements LoyaltyFamilyService {

    @Autowired
    LoyaltyFamilyRepository loyaltyFamilyRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    UserService userService;

    @Override
    public void deleteFamily(Long id) {
        loyaltyFamilyRepository.deleteById(id);
    }

    @Override
    public List<LoyaltyFamilyEntity> listFamily(Long orgId) {
        if (orgId > 0) {
            return loyaltyFamilyRepository.getByOrganization_Id(orgId);
        }
        return loyaltyFamilyRepository.findAll();
    }

    @Override
    public LoyaltyFamilyUpdateResponse updateFamily(LoyaltyFamilyDTO family) {
        validateFamily(family);

        LoyaltyFamilyEntity entity = createFamilyEntity(family);
        loyaltyFamilyRepository.save(entity);
        return new LoyaltyFamilyUpdateResponse(entity.getId());
    }

    @Override
    public void addNewMemberToFamily(Long userId, Long familyId) {
        userService.updateUserByFamilyId(familyId, userId);
    }

    @Override
    public Optional<LoyaltyFamilyEntity> getFamilyById(Long familyId) {
        return loyaltyFamilyRepository.findById(familyId);
    }

    @Override
    public List<UserEntity> getFamilyMembers(Long familyId) {
        return userService.getUsersByFamilyId(familyId);
    }

    private LoyaltyFamilyEntity createFamilyEntity(LoyaltyFamilyDTO family) {
        LoyaltyFamilyEntity entity = ofNullable(family)
                .map(LoyaltyFamilyDTO::getId)
                .map(this::getExistingFamily)
                .orElseGet(LoyaltyFamilyEntity::new);

        OrganizationEntity organization = securityService.getCurrentUserOrganization();

        entity.setOrganization(organization);
        entity.setFamilyName(family.getFamilyName());
        entity.setIsActive(family.getIsActive());
        return entity;
    }

    private LoyaltyFamilyEntity getExistingFamily(Long id) {
        return ofNullable(id)
                .flatMap(loyaltyFamilyRepository::findById)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
                        , FAMILY$PARAM$0001, id));
    }

    private void validateFamily(LoyaltyFamilyDTO family) {
        if (anyIsNull(family, family.getParentId())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , FAMILY$PARAM$0001, family.toString());
        }
    }

}
