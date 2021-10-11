package com.nasnav.dao;

import com.nasnav.persistence.ProductRating;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRatingRepository extends JpaRepository <ProductRating, Long> {

    Optional<ProductRating> findByIdAndVariant_ProductEntity_OrganizationId(Long id, Long orgId);

    Optional<ProductRating> findByVariant_IdAndUser_Id(Long variantId, Long userId);

    @Query("select r from ProductRating r" +
            " join fetch r.variant v" +
            " join fetch r.user u" +
            " where r.approved = false and v.productEntity.organizationId = :orgId" +
            " order by r.submissionDate desc")
    List<ProductRating> findUnapprovedVariantsRatings(@Param("orgId") Long orgId);

    @Query("select r from ProductRating r" +
            " join fetch r.variant v" +
            " join fetch r.user u" +
            " where v.id = :variantId and r.approved = true" +
            " order by r.submissionDate desc")
    List<ProductRating> findApprovedVariantRatings(@Param("variantId") Long variantId);

    @Query("select count (r) from ProductRating r " +
            " where r.approved = true and r.user.id = :userId")
    Integer countTotalRatingByUserId(@Param("userId") Long userId);

    @Query("select r from ProductRating r" +
            " join fetch r.variant v" +
            " join fetch v.productEntity p" +
            " join fetch OrganizationEntity org on p.organizationId = org.id" +
            " join fetch r.user u" +
            " where v.id = :variantId and r.approved = true and org.yeshteryState = 1" +
            " order by r.submissionDate desc")
    List<ProductRating> findApprovedYeshteryVariantRatings(@Param("variantId") Long variantId);
}
