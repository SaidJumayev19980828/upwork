package com.nasnav.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductsDeleteResponse {

    private boolean success;
    private List<Long> productIds;
}
