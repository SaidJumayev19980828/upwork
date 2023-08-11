package com.nasnav.test;


import com.nasnav.dto.Dates;
import com.nasnav.dto.Prices;
import com.nasnav.dto.Quantities;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.service.OrderStatisticService;
import com.nasnav.test.commons.test_templates.AbstractTestWithTempBaseDir;
import org.junit.jupiter.api.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.math.BigDecimal;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;

@RunWith(SpringRunner.class)
@Sql(executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD, scripts = {"/sql/Order_Statistics_Test_Data.sql"})
@Sql(executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD, scripts = {"/sql/database_cleanup.sql"})
class OrderStatisticServiceTest extends AbstractTestWithTempBaseDir {
    @Autowired
    private OrderStatisticService orderStatisticService;
    OrderSearchParam finalParams = new OrderSearchParam();


    @Test
    void testGettingOrderDatesStatistic() {
        Dates orderDatesStatistic = orderStatisticService.getOrderDatesStatistic(finalParams);
        assertThat(orderDatesStatistic, notNullValue());
        assertThat(orderDatesStatistic.getMaxCreatedDate(), equalTo("2022-02-05"));
        assertThat(orderDatesStatistic.getMinCreatedDate(), equalTo("2022-02-01"));
    }

    @Test
    void testGettingOrderQuantitiesStatistic() {
        Quantities orderQuantitiesStatistic = orderStatisticService.getOrderQuantitiesStatistic(finalParams);
        assertThat(orderQuantitiesStatistic, notNullValue());
        assertThat(orderQuantitiesStatistic.getMaxQuantity(), equalTo(14));
        assertThat(orderQuantitiesStatistic.getMinQuantity(), equalTo(1));
    }

    @Test
    void testGettingOrderPricesStatistic() {
        Prices orderPricesStatistic = orderStatisticService.getOrderPricesStatistic(finalParams);
        assertThat(orderPricesStatistic, notNullValue());
        assertThat(orderPricesStatistic.getMaxPrice(), equalTo(new BigDecimal("600.00")));
        assertThat(orderPricesStatistic.getMinPrice(), equalTo(new BigDecimal("50.00")));
    }
}
