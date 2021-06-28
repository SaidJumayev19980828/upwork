package com.nasnav.yeshtery.service;

import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.yeshtery.dao.YeshteryRecommendationRepository;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationRatingData;
import com.nasnav.yeshtery.persistence.YeshteryRecommendationSellingData;
import org.apache.mahout.cf.taste.impl.common.FastByIDMap;
import org.apache.mahout.cf.taste.impl.model.GenericDataModel;
import org.apache.mahout.cf.taste.impl.model.jdbc.PostgreSQLJDBCDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.EuclideanDistanceSimilarity;
import org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.model.JDBCDataModel;
import org.apache.mahout.cf.taste.model.PreferenceArray;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;
import org.postgresql.ds.PGPoolingDataSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class YeshteryRecommendationServiceImpl implements YeshteryRecommendationService {

    @Autowired
    private SecurityService securityService;

    @Autowired
    private YeshteryRecommendationRepository recommendationRepository;

    @Autowired
    DataSource dataSource;

    @Autowired
    ProductRepository productRepository;

    @Override
    public List<ProductEntity> getListOfSimilarityProducts(int recommendedItemsCount, int userId) {
        List<ProductEntity> items = new ArrayList<>();
        try {
            JDBCDataModel model = new PostgreSQLJDBCDataModel( dataSource  ,"orders_product_v" , "userid",  "itemid", "itemvalue", "created");
            ItemSimilarity itemSimilarity = new EuclideanDistanceSimilarity(model);
            Recommender itemRecommender = new GenericItemBasedRecommender(model, itemSimilarity);
            List<RecommendedItem> itemRecommendations = itemRecommender.recommend(userId, recommendedItemsCount);
            for (RecommendedItem item : itemRecommendations) {
                // add recommended items
                long prodId = item.getItemID();
                return productRepository.findByProductId(prodId, true)
                        .stream()
                        .collect(toList());
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
            JDBCDataModel model = new PostgreSQLJDBCDataModel( dataSource  ,"orders_product_v" , "userid",  "itemid", "itemvalue", "created");
            UserSimilarity similarity = new PearsonCorrelationSimilarity(model);
            UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.1, similarity, model);
            UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
            List<RecommendedItem> recommendations = recommender.recommend(userId, recommendedItemsCount);
            for (RecommendedItem item : recommendations) {
                long prodId = item.getItemID();
                return productRepository.findByProductId(prodId, true)
                        .stream()
                        .collect(toList());
            }
        } catch (Exception e) {
            items.clear();
            System.out.println(e);
        }
        return items;
    }

    @Override
    public List<YeshteryRecommendationSellingData> getListOfTopSellerProduct() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return recommendationRepository.findProductTopSelling(orgId);
    }

    @Override
    public List<YeshteryRecommendationSellingData> getListOfTopSellerProductByTag(Long tagId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return recommendationRepository.findProductTopSellingByTag(orgId, tagId);
    }

    @Override
    public List<YeshteryRecommendationSellingData> getListOfTopSellerProductByShop(Long shopId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return recommendationRepository.findProductTopSellingByShop(orgId, shopId);
    }

    @Override
    public List<YeshteryRecommendationRatingData> getListOfTopRatingProduct() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return recommendationRepository.findProductTopRating(orgId);
    }

    @Override
    public List<YeshteryRecommendationRatingData> getListOfTopRatingProductByTag(Long tagId) {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return recommendationRepository.findProductTopRatingByTag(orgId, tagId);
    }

}
