package com.nasnav.dao;

import com.nasnav.persistence.ThemeClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ThemeClassRepository extends JpaRepository<ThemeClassEntity, Integer> {

    List<ThemeClassEntity> findByIdIn(List<Integer> classIds);

    @Query("select count(c) from OrganizationEntity o left join o.themeClasses c" +
            " where o.id = :orgId")
    Integer countByOrganizationId(@Param("orgId") Long orgId);
}
