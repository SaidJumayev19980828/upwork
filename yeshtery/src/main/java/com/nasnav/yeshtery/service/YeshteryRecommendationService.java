package com.nasnav.yeshtery.service;

import com.nasnav.persistence.ProductEntity;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData;

import java.util.List;

public interface YeshteryRecommendationService {

    List<ProductEntity> getListOfSimilarityProducts(int recommendedItemsCount, int userId);
    List<ProductEntity> getListOfUserSimilarityItemOrders(int recommendedItemsCount, int userId);
    List<YeshteryRecommendationSellingData> getListOfTopSellerProduct(Long orgId);
    List<YeshteryRecommendationSellingData> getListOfTopSellerProductByTag(Long tagId, Long orgId);
    List<YeshteryRecommendationSellingData> getListOfTopSellerProductByShop(Long shopId, Long orgId);
    List<YeshteryRecommendationSellingData> getListOfTopSellerProductByShopTag(Long shopId, Long tagId, Long orgId);
    List<YeshteryRecommendationRatingData> getListOfTopRatingProduct(Long orgId);
    List<YeshteryRecommendationRatingData> getListOfTopRatingProductByTag(Long tagId, Long orgId);
}
