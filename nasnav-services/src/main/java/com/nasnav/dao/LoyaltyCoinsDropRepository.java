package com.nasnav.dao;

import com.nasnav.persistence.LoyaltyCoinsDropEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

public interface LoyaltyCoinsDropRepository extends JpaRepository<LoyaltyCoinsDropEntity, Long> {

    List<LoyaltyCoinsDropEntity> getByOrganization_Id(Long orgId);

    LoyaltyCoinsDropEntity getByOrganization_IdAndTypeId(Long orgId, Integer typeId);

    @Query("select coins "
            + " FROM LoyaltyCoinsDropEntity coins "
            + " LEFT JOIN coins.organization org "
            + " where org.id = :orgId "
            + " AND coins.typeId in :typeIds ")
    List<LoyaltyCoinsDropEntity> findByOrganization_IdAndTypeIdIn(@Param("orgId") Long orgId,
                                                                  @Param("typeIds") List<Integer> typeIds);

    List<LoyaltyCoinsDropEntity> findByOfficialVacationDateNotNullAndOrganization_IdIn(Set<Long> orgIds);


}
