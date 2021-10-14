package com.nasnav.service;


import com.nasnav.dto.request.FamilyDTO;
import com.nasnav.persistence.FamilyEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.FamilyUpdateResponse;

import java.util.List;
import java.util.Optional;

public interface FamilyService {

    void deleteFamily(Long id);
    List<FamilyEntity> listFamily(Long orgId);
    FamilyUpdateResponse updateFamily(FamilyDTO family);
    void addNewMemberToFamily(Long userId, Long familyId);
    Optional<FamilyEntity> getFamilyById(Long familyId);

    List<UserEntity> getFamilyMembers(Long familyId);
}
