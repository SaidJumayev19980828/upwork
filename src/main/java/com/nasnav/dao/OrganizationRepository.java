package com.nasnav.dao;

import java.util.List;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ThemeClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.repository.CrudRepository;

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
}



