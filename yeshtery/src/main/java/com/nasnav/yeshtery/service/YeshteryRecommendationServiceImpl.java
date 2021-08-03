package com.nasnav.yeshtery.service;

import com.nasnav.dao.ProductRepository;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.yeshtery.dao.YeshteryRecommendationRepository;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

@Service
public class YeshteryRecommendationServiceImpl implements YeshteryRecommendationService {

    @Autowired
    private YeshteryRecommendationRepository recommendationRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    ProductRepository productRepository;

    @Override
    public List<ProductEntity> getListOfSimilarity(int recommendedItemsCount, int userId) {
        List<ProductEntity> items = new ArrayList<>();
        try {
            JDBCDataModel model = new PostgreSQLJDBCDataModel( dataSource  ,"orders_product_v" , "userid",  "itemid", "itemid", "created");
            ItemSimilarity itemSimilarity = new EuclideanDistanceSimilarity(model);
            Recommender itemRecommender = new GenericItemBasedRecommender(model, itemSimilarity);
            List<RecommendedItem> itemRecommendations = itemRecommender.recommend(userId, recommendedItemsCount);
            for (RecommendedItem item : itemRecommendations) {
                long prodId = item.getItemID();
                items.add(productRepository.findProductDataById(prodId, true));
            }
            //
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            List<RecommendedItem> recommendations = recommender.recommend(userId, recommendedItemsCount);
            for (RecommendedItem item : recommendations) {
                long prodId = item.getItemID();
                if (items.contains(prodId)){
                    continue;
                }
                items.add(productRepository.findProductDataById(prodId, true));
            }
        } catch (Exception e) {
            items.clear();
            System.out.println(e);
        }
        return items;
    }

    @Override
    public List<YeshteryRecommendationSellingData> getListOfTopSellerProduct(Long shopId, Long tagId, Long orgId) {
        return recommendationRepository.findProductTopSelling(orgId, shopId, tagId);
    }

    @Override
    public List<YeshteryRecommendationRatingData> getListOfTopRatingProduct(Long orgId, Long tagId) {
        return recommendationRepository.findProductTopRating(orgId, tagId);
    }
}
