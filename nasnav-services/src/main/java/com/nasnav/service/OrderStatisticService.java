package com.nasnav.service;

import com.nasnav.dto.Dates;
import com.nasnav.dto.Prices;
import com.nasnav.dto.Quantities;
import com.nasnav.request.OrderSearchParam;


public interface OrderStatisticService {
    Dates getOrderDatesStatistic(OrderSearchParam finalParams);

    Prices getOrderPricesStatistic(OrderSearchParam finalParams);

    Quantities getOrderQuantitiesStatistic(OrderSearchParam finalParams);
}
