package com.nasnav.yeshtery.dao;

import com.nasnav.persistence.ProductEntity;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface YeshteryRecommendationRepository extends JpaRepository<ProductEntity, Long> {

    @Query("SELECT new com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData("
            + "product.id, product.name, count (rating.id), sum (rating.rate), (sum(rating.rate) / count (rating.id)) ) "
            + " FROM ProductRating rating "
            + "	LEFT JOIN rating.variant variants "
            + " LEFT JOIN variants.productEntity product "
            + " LEFT JOIN product.tags tag "
            + " WHERE ((coalesce(:orgId,-1) < 0) or product.organizationId = :orgId) and ((coalesce(:tagId,-1) < 0) or tag.id = :tagId)"
            + " group by product.id, product.name ")
    List<YeshteryRecommendationRatingData> findProductTopRating(@Param("orgId") Long orgId, @Param("tagId") Long tagId);

    @Query("SELECT new com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData("
            + "product.id, product.name, count (orders.id))"
            + " FROM BasketsEntity basket "
            + "	LEFT JOIN basket.stocksEntity stock "
            + " LEFT JOIN basket.ordersEntity orders "
            + " LEFT JOIN stock.productVariantsEntity variant "
            + " LEFT JOIN variant.productEntity product "
            + " LEFT JOIN orders.shopsEntity shop "
            + " LEFT JOIN product.tags tag "
            + " WHERE ((coalesce(:orgId,-1) < 0) or product.organizationId = :orgId) "
            + "   and ((coalesce(:shopId,-1) < 0) or shop.id = :shopId) "
            + "   and ((coalesce(:tagId,-1) < 0) or tag.id = :tagId) "
            + " group by product.id, product.name "
            + " order by count (orders.id) desc")
    List<YeshteryRecommendationSellingData> findProductTopSelling(@Param("orgId") Long orgId, @Param("shopId") Long shopId, @Param("tagId") Long tagId);
}
