package com.nasnav.response;

import com.nasnav.dto.DetailedOrderRepObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrdersListResponse {
    private Long total;
    private List<DetailedOrderRepObject> orders;
}