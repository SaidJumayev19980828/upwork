package com.nasnav.controller;

import com.nasnav.dto.request.RequestType;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.persistence.dto.query.result.CartStatisticsData;
import com.nasnav.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("orders")
    public List<Map<String,Object>> getOrderStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                        @RequestParam List<OrderStatus> statuses,
                                                        @RequestParam RequestType type,
                                                        @RequestParam (name = "months_count", required = false, defaultValue = "12") Integer months) {
        return statisticsService.getOrderStatistics(statuses, type, months);
    }

    @GetMapping("carts")
    public List<CartStatisticsData> getOrganizationCarts(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getOrganizationCarts();
    }

    @GetMapping("sold_products")
    public Map<String, List<ProductStatisticsInfo>> getProductsStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                        @RequestParam (name = "months_count", required = false, defaultValue = "12") Integer months) {
        return statisticsService.getProductsStatistics(months);
    }

    @GetMapping("sales")
    public Map getSalesStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                  @RequestParam(required = false) Integer month,
                                  @RequestParam(required = false) Integer week) {
        return statisticsService.getSalesStatistics(month, week);
    }

    @GetMapping("users")
    public Map getSalesStatistics(@RequestHeader(name = "User-Token", required = false) String userToken,
                                  @RequestParam(required = false) Integer month) {
        return statisticsService.getSalesStatisticsPerMonth(month);
    }

    @GetMapping("users/carts")
    public List getUsersAbandonedCarts(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getUsersAbandonedCarts();
    }

}
