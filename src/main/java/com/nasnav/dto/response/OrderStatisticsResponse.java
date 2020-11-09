package com.nasnav.dto.response;

import lombok.Data;
import java.util.List;

@Data
public class OrderStatisticsResponse {
    private String month;
    List<OrderStatisticsInfo> info;
}
