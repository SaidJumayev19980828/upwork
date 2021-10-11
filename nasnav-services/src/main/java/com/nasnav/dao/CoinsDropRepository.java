package com.nasnav.dao;

import com.nasnav.persistence.CoinsDropEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CoinsDropRepository extends JpaRepository<CoinsDropEntity, Long> {

    List<CoinsDropEntity> getByOrganization_Id(Long orgId);

    CoinsDropEntity getByOrganization_IdAndTypeId(Long orgId, Long typeId);

    @Query("select coins "
            + " FROM CoinsDropEntity coins "
            + " LEFT JOIN coins.organization org "
            + " where org.id = :orgId "
            + " AND coins.typeId in :typeIds ")
    List<CoinsDropEntity> findByOrganization_IdAndTypeIdIn(@Param("orgId") Long orgId,
                                                            @Param("typeIds") List<Integer> typeIds);

    List<CoinsDropEntity> findByOfficialVacationDateNotNullAndOrganization_Id(Long orgId);


}
