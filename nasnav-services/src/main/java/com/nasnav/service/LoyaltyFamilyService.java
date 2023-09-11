package com.nasnav.service;


import com.nasnav.dto.request.LoyaltyFamilyDTO;
import com.nasnav.persistence.LoyaltyFamilyEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.response.LoyaltyFamilyUpdateResponse;

import java.util.List;
import java.util.Optional;

public interface LoyaltyFamilyService {

    void deleteFamily(Long id);
    List<LoyaltyFamilyEntity> listFamily(Long orgId);
    LoyaltyFamilyUpdateResponse updateFamily(LoyaltyFamilyDTO family);
    List<UserEntity> addNewMemberToFamily(Long userId, Long familyId);
    Optional<LoyaltyFamilyEntity> getFamilyById(Long familyId);

    List<UserEntity> getFamilyMembers(Long familyId);
    List<LoyaltyFamilyEntity> listFamily();
}
