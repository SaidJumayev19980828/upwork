package com.nasnav.dao;

import com.nasnav.persistence.VariantFeatureValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Set;

public interface VariantFeatureValuesRepository extends JpaRepository<VariantFeatureValueEntity, Long> {

    @Query("select featureValue from VariantFeatureValueEntity featureValue " +
            " left join fetch featureValue.feature feature " +
            " left join fetch featureValue.variant variant " +
            " where feature.organization.id = :orgId")
    Set<VariantFeatureValueEntity> findByOrganizationId(@Param("orgId") Long orgId);
}
