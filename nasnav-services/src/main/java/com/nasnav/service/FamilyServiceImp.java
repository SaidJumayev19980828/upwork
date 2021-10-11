package com.nasnav.service;

import com.nasnav.dao.FamilyRepository;
import com.nasnav.dto.request.FamilyDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.FamilyEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.FamilyUpdateResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service
public class FamilyServiceImp implements FamilyService {

    @Autowired
    FamilyRepository familyRepository;
    @Autowired
    SecurityService securityService;
    @Autowired
    UserService userService;

    @Override
    public void deleteFamily(Long id) {
        familyRepository.deleteById(id);
    }

    @Override
    public List<FamilyEntity> listFamily() {
        return familyRepository.findAll();
    }

    @Override
    public List<FamilyEntity> listFamilyByOrgId(Long orgId) {
        return familyRepository.getByOrganization_Id(orgId);
    }

    @Override
    public FamilyUpdateResponse updateFamily(FamilyDTO family) {
        validateFamily(family);

        FamilyEntity entity = createFamilyEntity(family);
        familyRepository.save(entity);
        return new FamilyUpdateResponse(entity.getId());
    }

    @Override
    public void addNewMemberToFamily(Long userId, Long familyId) {
        userService.updateUserByFamilyId(familyId, userId);
    }

    @Override
    public Optional<FamilyEntity> getFamilyById(Long familyId) {
        return familyRepository.findById(familyId);
    }

    @Override
    public List<UserEntity> getFamilyMembers(Long familyId) {
        return userService.getUsersByFamilyId(familyId);
    }

    private FamilyEntity createFamilyEntity(FamilyDTO family) {
        FamilyEntity entity = ofNullable(family)
                .map(FamilyDTO::getId)
                .map(this::getExistingFamily)
                .orElseGet(FamilyEntity::new);

        OrganizationEntity organization = securityService.getCurrentUserOrganization();

        entity.setOrganization(organization);
        entity.setFamilyName(family.getFamilyName());
        entity.setIsActive(family.getIsActive());
        return entity;
    }

    private FamilyEntity getExistingFamily(Long id) {
        return ofNullable(id)
                .flatMap(familyRepository::findById)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE
                        , FAMILY$PARAM$0001, id));
    }

    private void validateFamily(FamilyDTO family) {
        if (anyIsNull(family, family.getParentId())) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE
                    , FAMILY$PARAM$0001, family.toString());
        }
    }

}
