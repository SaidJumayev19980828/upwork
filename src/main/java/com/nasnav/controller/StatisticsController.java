package com.nasnav.controller;

import com.nasnav.dto.response.OrderStatisticsInfo;
import com.nasnav.dto.response.navbox.CartItem;
import com.nasnav.service.StatisticsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/statistics")
public class StatisticsController {

    @Autowired
    private StatisticsService statisticsService;

    @GetMapping("orders")
    public Map<Date, List<OrderStatisticsInfo>> getOrderStatistics(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getOrderStatistics();
    }

    @GetMapping("carts")
    public Map<Long, List<CartItem>> getOrganizationCarts(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return statisticsService.getOrganizationCarts();
    }
}
