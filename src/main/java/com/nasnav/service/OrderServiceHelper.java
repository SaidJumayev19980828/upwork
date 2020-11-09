package com.nasnav.service;

import com.nasnav.commons.utils.CollectionUtils;
import com.nasnav.dao.BasketRepository;
import com.nasnav.dto.BasketItemDetails;
import com.nasnav.persistence.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static java.util.Collections.emptyMap;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Service
public class OrderServiceHelper {

    @Autowired
    private ProductImageService imgService;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private SecurityService securityService;



    public Map<Long, Optional<String>> getVariantsImagesList(MetaOrderEntity order) {
        List<Long> variantsIds =
                order
                .getSubOrders()
                .stream()
                .map(OrdersEntity::getBasketsEntity)
                .flatMap(Set::stream)
                .map(BasketsEntity::getStocksEntity)
                .map(StocksEntity::getProductVariantsEntity)
                .map(ProductVariantsEntity::getId)
                .collect(toList());
        return imgService.getVariantsCoverImages(variantsIds);
    }




    public Map<Long, Optional<String>> getVariantsImagesList(List<BasketItemDetails> basketItems) {
        List<Long> variantsIds =
                basketItems
                .stream()
                .map(BasketItemDetails::getVariantId)
                .collect(toList());
        return imgService.getVariantsCoverImages(variantsIds);
    }




    public Map<Long, BasketsEntity> getBasketsMap(List<Long> ids) {
        if(ids.isEmpty()){
            return emptyMap();
        }
        Long orgId = securityService.getCurrentUserOrganizationId();
        return mapInBatches(ids, 500, batch -> basketRepository.findByIdIn(batch, orgId))
                .stream()
                .collect(toMap(BasketsEntity::getId, b -> b));
    }
}
