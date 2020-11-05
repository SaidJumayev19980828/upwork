package com.nasnav.service.helpers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.response.navbox.CartItem;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import static java.util.Collections.emptyMap;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;

@Service
public class CartServiceHelper {
    private static final String ADDITIONAL_DATA_COLLECTION_ID = "collection_id";
    private static final Logger logger = LogManager.getLogger();

    @Autowired
    private ObjectMapper objectMapper;


    public String getAdditionalDataJsonString(CartItem item) {
        try {
            return objectMapper.writeValueAsString(item.getAdditionalData());
        } catch (JsonProcessingException e) {
            logger.error(e,e);
            return "{}";
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
     * if the cart item has additional data with key "collection_id", the value of
     * the collection id will replace productId.
     * This is a workaround that was requested by the frontend team due to a just-before-launch
     * requirement to show collections thumbnails in the cart.
     * */
    public void replaceProductIdWithCollectionId(CartItem item) {
        getCollectionId(item)
                .ifPresent(item::setProductId);
    }



    private Optional<Long> getCollectionId(CartItem item) {
        return ofNullable(item)
                .map(CartItem::getAdditionalData)
                .map(additionalData -> additionalData.get(ADDITIONAL_DATA_COLLECTION_ID))
                .flatMap(this::parseCollectionId);
    }



    private Optional<Long> parseCollectionId(Object object) {
        try{
            if(object == null){
                return empty();
            }else {
                return Optional.of(Long.valueOf(object.toString()));
            }
        }catch(Throwable e){
            logger.error(e,e);
            return empty();
        }
    }
}
