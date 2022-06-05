package com.nasnav.dao;

import com.nasnav.persistence.VariantFeatureValueEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface VariantFeatureValuesRepository extends JpaRepository<VariantFeatureValueEntity, Long> {

    @Query("select featureValue from VariantFeatureValueEntity featureValue " +
            " left join fetch featureValue.feature feature " +
            " left join fetch featureValue.variant variant " +
            " where feature.organization.id = :orgId")
    Set<VariantFeatureValueEntity> findByOrganizationId(@Param("orgId") Long orgId);

    @Query("select featureValue from VariantFeatureValueEntity featureValue " +
            " left join fetch featureValue.feature feature " +
            " left join fetch featureValue.variant variant " +
            " where feature.organization.id = :orgId and variant.id = :variantId and feature.id = :featureId")
    Optional<VariantFeatureValueEntity> findByVariantIdAndFeatureIdAndOrganizationId(
            @Param("orgId") Long orgId,
            @Param("variantId") Long variantId,
            @Param("featureId") Integer featureId);

    @Query("select featureValue from VariantFeatureValueEntity featureValue " +
            " left join fetch featureValue.feature feature " +
            " left join fetch featureValue.variant variant " +
            " where feature.organization.id = :orgId and feature.id = :featureId")
    Set<VariantFeatureValueEntity> findByFeatureIdAndOrganizationId(
            @Param("orgId") Long orgId,
            @Param("featureId") Integer featureId);

    @Query("select feature.id from VariantFeatureValueEntity featureValues " +
            " left join featureValues.feature feature " +
            " left join featureValues.variant variant " +
            " left join variant.productEntity product " +
            " where feature.id = :featureId and product.organizationId = :orgId and variant.removed = 0")
    List<Long> findByFeature(@Param("featureId")Integer featureId, @Param("orgId") Long orgId);

    @Transactional
    @Modifying
    void deleteByFeature_Id(Integer featureId);
}
