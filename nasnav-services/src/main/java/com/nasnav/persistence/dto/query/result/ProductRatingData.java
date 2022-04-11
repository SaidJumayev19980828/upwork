package com.nasnav.persistence.dto.query.result;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductRatingData {
    private Long productId;
    private Double average;
}
