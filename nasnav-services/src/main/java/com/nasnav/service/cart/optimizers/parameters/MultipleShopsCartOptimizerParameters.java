package com.nasnav.service.cart.optimizers.parameters;

import lombok.Data;

import java.util.Map;

@Data
public class MultipleShopsCartOptimizerParameters {
    private Map<Long, Long> orgShops;
}
