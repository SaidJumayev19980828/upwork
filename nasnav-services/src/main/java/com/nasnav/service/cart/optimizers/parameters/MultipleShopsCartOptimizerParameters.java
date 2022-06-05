package com.nasnav.service.cart.optimizers.parameters;

import lombok.Data;

import java.util.List;

@Data
public class MultipleShopsCartOptimizerParameters {
    private List<Long> orgShops;
}
