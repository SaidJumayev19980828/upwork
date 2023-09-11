package com.nasnav.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.MetaOrderEntity;
import com.nasnav.persistence.OrdersEntity;

public interface OrderServiceHelper {

  Map<Long, Optional<String>> getVariantsImagesList(MetaOrderEntity order);

  Map<Long, Optional<String>> getVariantsImagesList(Set<OrdersEntity> orders);

  Map<Long, BasketsEntity> getBasketsMap(List<Long> ids);

}