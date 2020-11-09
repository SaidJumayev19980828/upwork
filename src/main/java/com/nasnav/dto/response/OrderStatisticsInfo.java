package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.nasnav.enumerations.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatisticsInfo {
    private Date date;
    private Integer statusInt;
    private String status;
    private Long count;
    private BigDecimal income;

    public OrderStatisticsInfo(Date date, Integer statusInt, Long count, BigDecimal income) {
        this.date = date;
        this.status = OrderStatus.findEnum(statusInt).name();
        this.income = income;
        this.count = count;
    }

    public OrderStatisticsInfo(String status, Long count, BigDecimal income) {
        this.status = status;
        this.income = income;
        this.count = count;
    }
}
