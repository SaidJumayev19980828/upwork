package com.nasnav.controller;

import com.nasnav.dto.request.RequestType;
import com.nasnav.dto.response.OrderStatisticsInfo;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("orders")
    public Map<Date, List<OrderStatisticsInfo>> getOrderStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                                   @RequestParam List<OrderStatus> statuses,
                                                                   @RequestParam RequestType type) {
        return statisticsService.getOrderStatistics(statuses, type);
    }

    @GetMapping("carts")
    public Map<Long, List<CartItem>> getOrganizationCarts(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getOrganizationCarts();
    }

    @GetMapping("sold_products")
    public Map<Date, List<ProductStatisticsInfo>> getProductsStatistics(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getProductsStatistics();
    }
}
