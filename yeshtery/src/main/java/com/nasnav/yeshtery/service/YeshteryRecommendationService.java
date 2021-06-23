package com.nasnav.yeshtery.service;

import com.nasnav.persistence.ProductEntity;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData;

import java.util.List;

public interface YeshteryRecommendationService {

    List<ProductEntity> getListOfSimilarityProducts(int recommendedItemsCount, int userId);
    List<ProductEntity> getListOfUserSimilarityItemOrders(int recommendedItemsCount, int userId);
    List<YeshteryRecommendationSellingData> getListOfTopSellerProduct();
    List<YeshteryRecommendationSellingData> getListOfTopSellerProductByTag(Long tagId);
    List<YeshteryRecommendationSellingData> getListOfTopSellerProductByShop(Long shopId);
    List<YeshteryRecommendationRatingData> getListOfTopRatingProduct();
    List<YeshteryRecommendationRatingData> getListOfTopRatingProductByTag(Long tagId);
}
