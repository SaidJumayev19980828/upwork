package com.nasnav.service;

import java.util.List;
import java.util.Map;

import com.nasnav.dto.UserCartInfo;
import com.nasnav.dto.request.RequestType;
import com.nasnav.dto.response.ProductStatisticsInfo;
import com.nasnav.dto.response.VideoCallStatsResponse;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.persistence.CartItemEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.dto.query.result.CartStatisticsData;

public interface StatisticsService {

  List<Map<String, Object>> getOrderStatistics(List<OrderStatus> statuses, RequestType type, Integer months);

  List<CartStatisticsData> getOrganizationCarts();

  Map<String, List<ProductStatisticsInfo>> getProductsStatistics(Integer months);

  Map getSalesStatistics(Integer monthNumber, Integer week);

  Map getSalesStatisticsPerMonth(Integer monthNumber);

  List<UserCartInfo> getUsersAbandonedCarts();

  UserCartInfo toUserCartInfo(Map.Entry<UserEntity, List<CartItemEntity>> map);

  VideoCallStatsResponse getVideoCallStats();


}