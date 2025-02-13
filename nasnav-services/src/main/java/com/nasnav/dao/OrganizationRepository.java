package com.nasnav.dao;

import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ThemeClassEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.Set;
public interface OrganizationRepository extends JpaRepository<OrganizationEntity, Long> {

    List<OrganizationEntity> findByName(String name);

    OrganizationEntity findOneByName(String name);

    OrganizationEntity findOneByNameIgnoreCase(String name);

    OrganizationEntity findOneById(Long id);

    boolean existsByIdAndYeshteryState(Long id, Integer yeshteryState);

    Optional<OrganizationEntity> findByIdAndYeshteryState(Long id, Integer yeshteryState);

    OrganizationEntity findByPname(String pname);

    OrganizationEntity findByPnameAndYeshteryState(String pname, Integer yeshteryState);

    @Query("select o.id from OrganizationEntity o where o.themeId = :themeId")
    Set<Long> findByThemeId(@Param("themeId") Integer themeId);

    List<OrganizationEntity> findByYeshteryState(Integer yeshteryState);

    @Query("select o.id from OrganizationEntity o where o.yeshteryState = :state")
    Set<Long> findIdByYeshteryState(@Param("state") Integer yeshteryState);

    @Query("select o.id from OrganizationEntity o where o.yeshteryState = :state and o.id = :orgId")
    Set<Long> findIdByYeshteryStateAndOrganizationId(@Param("state") Integer yeshteryState, @Param("orgId") Long orgId);

    @Query("select distinct o from OrganizationEntity o left join fetch o.shops shop where o.yeshteryState = 1 order by o.priority desc")
    List<OrganizationEntity> findYeshteryOrganizations();

    @Query("select distinct o from OrganizationEntity o " +
            " left join TagsEntity t on t.organizationEntity.id = o.id " +
            " left join fetch o.shops shop " +
            " where o.yeshteryState = 1 and t.categoriesEntity.id in :ids" +
            " order by o.priority desc")
    List<OrganizationEntity> findYeshteryOrganizationsFilterByCategory(@Param("ids") List<Long> ids);

    OrganizationEntity findByIdAndThemeId(Long id, Integer themeId);

    boolean existsByIdAndThemeId(Long id, Integer themeId);

    Integer countByThemeClassesContains(ThemeClassEntity themeClass);

    @Query("select count(c) from OrganizationEntity o left join o.themeClasses c where o.id = :orgId and c.id = :classId")
    Integer countThemeClassesByOrganizationId(@Param("orgId") Long orgId,
                                              @Param("classId") Integer classId);

    @Query("SELECT org from OrganizationEntity org order by org.priority desc")
    List<OrganizationEntity> findAllOrganizations();
}



