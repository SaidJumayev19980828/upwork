package com.nasnav.service.impl;

import com.nasnav.dao.BasketRepository;
import com.nasnav.persistence.*;
import com.nasnav.service.OrderServiceHelper;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.SecurityService;

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
public class OrderServiceHelperImpl implements OrderServiceHelper {

    @Autowired
    private ProductImageService imgService;

    @Autowired
    private BasketRepository basketRepository;

    @Autowired
    private SecurityService securityService;



    @Override
    public Map<Long, Optional<String>> getVariantsImagesList(MetaOrderEntity order) {
        Set<OrdersEntity> orders = order.getSubOrders();
        return getVariantsImagesList(orders);
    }


    @Override
    public Map<Long, Optional<String>> getVariantsImagesList(Set<OrdersEntity> orders) {
        List<Long> variantsIds =
                orders
                .stream()
                .map(OrdersEntity::getBasketsEntity)
                .flatMap(Set::stream)
                .map(BasketsEntity::getStocksEntity)
                .map(StocksEntity::getProductVariantsEntity)
                .map(ProductVariantsEntity::getId)
                .collect(toList());
        return imgService.getVariantsCoverImages(variantsIds);
    }


    @Override
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
