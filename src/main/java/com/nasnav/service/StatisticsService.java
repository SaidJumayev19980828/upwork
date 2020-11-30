package com.nasnav.service;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.MetaOrderRepository;
import com.nasnav.dao.OrdersRepository;
import com.nasnav.dto.request.RequestType;
import com.nasnav.dto.response.OrderStatisticsInfo;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.OrderStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.AbstractMap.SimpleEntry;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.*;

@Service
public class StatisticsService {

    @Autowired
    private SecurityService securityService;
    @Autowired
    private CartService cartService;

    @Autowired
    private OrdersRepository ordersRepo;
    @Autowired
    private MetaOrderRepository metaOrderRepo;
    @Autowired
    private CartItemRepository cartItemRepo;

    public List<OrderStatisticsInfo> getOrderStatistics(List<OrderStatus> statuses, RequestType type) {
        if (type == null || statuses == null || statuses.isEmpty())
            return null;
        Long orgId = securityService.getCurrentUserOrganizationId();
        List<Integer> statusesIntegers = statuses.stream().map(status -> status.getValue()).collect(toList());
        return metaOrderRepo.getOrderStatisticsPerMonth(orgId, statusesIntegers)
                .stream()
                .collect(groupingBy(OrderStatisticsInfo::getDate));
    }

    private Map.Entry<Date, List<OrderStatisticsInfo>> toOrderStatisticsInfo(Map.Entry<Date, List<OrderStatisticsInfo>> entry) {
        return new SimpleEntry<Date, List<OrderStatisticsInfo>>(entry.getKey(), entry.getValue()
                .stream()
                .map(i -> new OrderStatisticsInfo(i.getStatus(), i.getCount(), i.getIncome()))
                .collect(toList()));
    }


    public Map<Long, List<CartItem>> getOrganizationCarts() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return cartService.toCartItemsDto(cartItemRepo.findCartsByOrg_Id(orgId))
                .stream()
                .collect(groupingBy(CartItem::getUserId));
    }

    public Map<Date, List<ProductStatisticsInfo>> getProductsStatistics() {
        Long orgId = securityService.getCurrentUserOrganizationId();
        return ordersRepo.getProductsStatisticsPerMonth(orgId)
                .stream()
                .collect(groupingBy(ProductStatisticsInfo::getDate));
    }
}
