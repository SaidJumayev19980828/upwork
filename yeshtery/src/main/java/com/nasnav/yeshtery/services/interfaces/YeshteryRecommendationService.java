package com.nasnav.yeshtery.services.interfaces;

import com.nasnav.persistence.ProductEntity;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData;

import java.util.List;

public interface YeshteryRecommendationService {
    List<ProductEntity> getListOfSimilarity(int recommendedItemsCount, int userId);
    List<YeshteryRecommendationSellingData> getListOfTopSellerProduct(Long shopId, Long tagId, Long orgId);
    List<YeshteryRecommendationRatingData> getListOfTopRatingProduct(Long orgId, Long tagId);
}
