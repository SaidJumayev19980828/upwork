package com.nasnav.dao;

import com.nasnav.persistence.SeoKeywordEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


public interface SeoKeywordRepository extends JpaRepository<SeoKeywordEntity,Long> {

    @Query("SELECT seo FROM SeoKeywordEntity seo " +
            " LEFT JOIN FETCH seo.organization org " +
            " WHERE org.id = :orgId")
    List<SeoKeywordEntity> findByOrganization_Id(@Param("orgId")Long orgId);


    @Transactional
    @Modifying
    @Query("DELETE FROM SeoKeywordEntity seo" +
            " WHERE seo.entityId = :entityId " +
            " AND seo.typeId = :typeId " +
            " AND seo.organization.id = :orgId ")
    void deleteByEntityIdAndTypeAndOrganization_Id(
            @Param("entityId")Long entityId,
            @Param("typeId")Integer typeId,
            @Param("orgId")Long currentUserOrg);


    @Query("SELECT seo FROM SeoKeywordEntity seo " +
            " LEFT JOIN FETCH seo.organization org " +
            " WHERE seo.entityId = :entityId " +
            " AND seo.typeId = :typeId " +
            " AND org.id = :orgId")
    List<SeoKeywordEntity> findByEntityIdAndTypeIdAndOrganization_Id
            (@Param("entityId")Long entityId,
             @Param("typeId")Integer typeId,
             @Param("orgId")Long orgId);

    @Query("SELECT seo FROM SeoKeywordEntity seo " +
            " WHERE seo.entityId = :entityId " +
            " AND seo.typeId = :typeId ")
    List<SeoKeywordEntity> findByEntityIdAndTypeId(@Param("entityId")Long entityId, @Param("typeId")Integer typeId);

    List<SeoKeywordEntity> findByEntityIdInAndTypeId(List<Long> entityIdList, Integer type);

}
