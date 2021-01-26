package com.nasnav.dao;

import java.util.List;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ThemeClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    List<OrganizationEntity> findByName(String name);

    OrganizationEntity findOneByName(String name);

    OrganizationEntity findOneByNameIgnoreCase(String name);

    OrganizationEntity findOneById(Long id);

    OrganizationEntity findByPname(String pname);

    List<OrganizationEntity> findByThemeId(Integer themeId);

    OrganizationEntity findByIdAndThemeId(Long id, Integer themeId);

    boolean existsByIdAndThemeId(Long id, Integer themeId);

    Integer countByThemeClassesContains(ThemeClassEntity themeClass);

    @Query("select count(c) from OrganizationEntity o left join o.themeClasses c where o.id = :orgId and c.id = :classId")
    Integer countThemeClassesByOrganizationId(@Param("orgId") Long orgId,
                                              @Param("classId") Integer classId);

    @Query("SELECT org.id from OrganizationEntity org")
    List<Long> findAllOrganizations();
}



