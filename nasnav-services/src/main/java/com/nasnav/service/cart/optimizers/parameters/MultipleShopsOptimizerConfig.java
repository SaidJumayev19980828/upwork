package com.nasnav.service.cart.optimizers.parameters;

import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class MultipleShopsOptimizerConfig {
    private Map<Long, Set<Long>> orgShops;
}
