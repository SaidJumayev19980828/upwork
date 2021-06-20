package com.nasnav.yeshtery.service;

import com.nasnav.persistence.ProductEntity;

import java.util.List;

public interface YeshteryRecommendationService {

    List<ProductEntity> getListOfSimilarityProducts(int recommendedItemsCount, int userId);
    List<ProductEntity> getListOfUserSimilarityItemOrders(int recommendedItemsCount, int userId);
    List<ProductEntity> getListOfTopSellerProduct();
    List<ProductEntity> getListOfTopSellerProductByTag(int tagId);
    List<ProductEntity> getListOfTopRatingProduct();
    List<ProductEntity> getListOfTopRatingProductByTag(int tagId);
}
