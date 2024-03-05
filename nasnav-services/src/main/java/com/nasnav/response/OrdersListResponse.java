package com.nasnav.response;

import com.nasnav.dto.DetailedOrderRepObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OrdersListResponse {
    private Long total;
    private List<DetailedOrderRepObject> orders;
}