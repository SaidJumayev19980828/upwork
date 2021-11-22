package com.nasnav.service.cart.optimizers.parameters;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.Map;
import java.util.Set;

@Data
public class MultipleShopsOptimizerConfig {
    @JsonProperty(value = "ORGS_WITH_SHOPS_MAP")
    private Map<Long, Set<Long>> orgShops;
}
