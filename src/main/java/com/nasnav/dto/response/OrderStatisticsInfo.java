package com.nasnav.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.text.DateFormatSymbols;


@Data
@AllArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderStatisticsInfo {
    private Date date;
    private String month;
    private String name;
    private String status;
    @JsonProperty("count_per_status")
    private Map<String, Long> statusToCount;
    @JsonProperty("total_per_status")
    private Map<String, BigDecimal> statusToTotal;
    private Long count;
    @JsonProperty("total")
    private BigDecimal income;

    public OrderStatisticsInfo(Date date, Integer statusInt, BigDecimal income) {
        this.date = date;
        this.status = OrderStatus.findEnum(statusInt).name();
        this.income = income;
    }

    public OrderStatisticsInfo(Date date, Map<String, Long> statusToCount, Map<String, BigDecimal> statusToTotal) {
        this.month = DateFormatSymbols.getInstance().getMonths()[date.getMonth()-1];
        this.statusToCount = statusToCount;
        this.statusToTotal = statusToTotal;
    }

    public OrderStatisticsInfo(Date date, Integer statusInt, Long count) {
        this.date = date;
        this.status = OrderStatus.findEnum(statusInt).name();
        this.count = count;
    }
}
