package com.nasnav.yeshtery.service;

import com.nasnav.persistence.ProductEntity;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import java.util.ArrayList;
import java.util.List;

public class YeshteryRecommendationServiceImpl implements YeshteryRecommendationService {

    @Override
    public List<ProductEntity> getListOfSimilarityProducts(int recommendedItemsCount, int userId) {
        List<ProductEntity> items = new ArrayList<>();
        try {
            FastByIDMap<PreferenceArray> products = new FastByIDMap<>();
            // Data
            DataModel model = new GenericDataModel(products);
            ItemSimilarity itemSimilarity = new EuclideanDistanceSimilarity(model);
            Recommender itemRecommender = new GenericItemBasedRecommender(model, itemSimilarity);
            List<RecommendedItem> itemRecommendations = itemRecommender.recommend(userId, recommendedItemsCount);
            for (RecommendedItem item : itemRecommendations) {
                // add recommended items
            }
        } catch (Exception e) {
            items.clear();
            System.out.println(e);
        }
        return items;
    }

    @Override
    public List<ProductEntity> getListOfUserSimilarityItemOrders(int recommendedItemsCount, int userId) {
        List<ProductEntity> items = new ArrayList<>();
        try {
            FastByIDMap<PreferenceArray> products = new FastByIDMap<>();
            // Data
            DataModel model = new GenericDataModel(products);
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            List<RecommendedItem> recommendations = recommender.recommend(userId, recommendedItemsCount);
            for (RecommendedItem item : recommendations) {
                // add recommended data
            }
        } catch (Exception e) {
            items.clear();
            System.out.println(e);
        }
        return items;
    }

    @Override
    public List<ProductEntity> getListOfTopSellerProduct() {
        return null;
    }

    @Override
    public List<ProductEntity> getListOfTopSellerProductByTag(int tagId) {
        return null;
    }

    @Override
    public List<ProductEntity> getListOfTopRatingProduct() {
        return null;
    }

    @Override
    public List<ProductEntity> getListOfTopRatingProductByTag(int tagId) {
        return null;
    }

}
