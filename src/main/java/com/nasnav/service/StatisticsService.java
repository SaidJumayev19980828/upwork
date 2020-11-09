package com.nasnav.service;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dto.response.OrderStatisticsInfo;
import com.nasnav.dto.response.navbox.CartItem;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;

@Service
public class StatisticsService {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private CartService cartService;

    @Autowired
    private MetaOrderRepository metaOrderRepo;
    @Autowired
    private CartItemRepository cartItemRepo;

    public Map<Date, List<OrderStatisticsInfo>> getOrderStatistics() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return metaOrderRepo.getOrderStatisticsPerMonth(orgId)
                .stream()
                .collect(groupingBy(OrderStatisticsInfo::getDate))
                .entrySet()
                .stream()
                .map(e ->  new SimpleEntry<Date, List<OrderStatisticsInfo>>(e.getKey(), e.getValue()
                            .stream()
                            .map(i -> new OrderStatisticsInfo(i.getStatus(), i.getCount(), i.getIncome()))
                            .collect(Collectors.toList())))
                .collect(toMap(Map.Entry::getKey, Map.Entry::getValue));

    }


    public Map<Long, List<CartItem>> getOrganizationCarts() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return cartService.toCartItemsDto(cartItemRepo.findCartsByOrg_Id(orgId))
                .stream()
                .collect(groupingBy(CartItem::getUserId));
    }
}
