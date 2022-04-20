package com.nasnav.service.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.persistence.CartItemEntity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.Optional.ofNullable;

@Service
public class CartServiceHelper {
    public static final String ADDITIONAL_DATA_PRODUCT_ID = "product_id";
    public static final String ADDITIONAL_DATA_PRODUCT_TYPE = "product_type";
    public static final String ADDITIONAL_DATA_IS_OUT_OF_STOCK = "out_of_stock";
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ObjectMapper objectMapper;


    public String getAdditionalDataJsonString(CartItem item, Integer stockQuantity) {
        try {
            Map<String,Object> additionalDataMap =
                    ofNullable(item.getAdditionalData())
                        .map(HashMap::new)
                        .orElseGet(HashMap::new);
            ofNullable(item.getProductId())
                    .ifPresent(id -> additionalDataMap.put(ADDITIONAL_DATA_PRODUCT_ID, id));
            ofNullable(item.getProductType())
                    .ifPresent(id -> additionalDataMap.put(ADDITIONAL_DATA_PRODUCT_TYPE, id));
            if (stockQuantity == null || stockQuantity == 0) {
                additionalDataMap.put(ADDITIONAL_DATA_IS_OUT_OF_STOCK, true);
            }
            return objectMapper.writeValueAsString(additionalDataMap);
        } catch (JsonProcessingException e) {
            logger.error(e,e);
            return "{}";
        }
    }

    public void addOutOfStockFlag(CartItemEntity entity) {
        Map<String,Object> additionalData = getAdditionalDataAsMap(entity.getAdditionalData());
        additionalData.put(ADDITIONAL_DATA_IS_OUT_OF_STOCK, true);
        try {
            entity.setAdditionalData(objectMapper.writeValueAsString(additionalData));
        } catch (JsonProcessingException e) {
            logger.error(e,e);
        }
    }

    public Map<String,Object> getAdditionalDataAsMap(String json) {
        String jsonString = ofNullable(json).orElse("{}");
        try {
            return objectMapper.readValue(jsonString, new TypeReference<Map<String,Object>>(){});
        } catch (IOException e) {
            logger.error(e,e);
            return emptyMap();
        }
    }



    /**
     * if the cart item has additional data with key "product_id", the value in additionalData will replace
     * CartItem.productId
     * This is a workaround that was requested by the frontend team due to a just-before-launch
     * requirement to show collections thumbnails in the cart.
     * */
    public void replaceProductIdWithGivenProductId(CartItem item) {
        getProductIdFromAdditionalData(item)
                .ifPresent(item::setProductId);
    }



    private Optional<Long> getProductIdFromAdditionalData(CartItem item) {
        return ofNullable(item)
                .map(CartItem::getAdditionalData)
                .map(additionalData -> additionalData.get(ADDITIONAL_DATA_PRODUCT_ID))
                .flatMap(EntityUtils::parseLongSafely);
    }







    /**
     * if the cart item has additional data with key "product_type", the value in additionalData will replace
     * CartItem.productType
     * This is a workaround that was requested by the frontend team due to a just-before-launch
     * requirement to show collections thumbnails in the cart.
     * */
    public void addProductTypeFromAdditionalData(CartItem item) {
        getProductType(item)
                .ifPresent(item::setProductType);
    }



    private Optional<Integer> getProductType(CartItem item) {
        return ofNullable(item)
                .map(CartItem::getAdditionalData)
                .map(additionalData -> additionalData.get(ADDITIONAL_DATA_PRODUCT_TYPE))
                .flatMap(EntityUtils::parseIntegerSafely);
    }
}
