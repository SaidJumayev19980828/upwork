package com.nasnav.service.cart.optimizers;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

import java.util.Objects;
import java.util.Optional;

import static java.util.Arrays.asList;
import static java.util.Arrays.stream;

public enum CartOptimizationStrategy {
	SAME_CITY(OptimizationStratigiesNames.SAME_CITY)
	, DEFAULT_OPTIMIZER(OptimizationStratigiesNames.SAME_CITY)
	, WAREHOUSE(OptimizationStratigiesNames.WAREHOUSE)
	, TRANSPARENT(OptimizationStratigiesNames.TRANSPARENT)
	, SHOP_PER_SUBAREA(OptimizationStratigiesNames.SHOP_PER_SUBAREA)
	, MULTIPLE_SHOPS(OptimizationStratigiesNames.MULTIPLE_SHOPS);
	
	@Getter
	@JsonValue
    private final String value;
	
	@JsonCreator
	CartOptimizationStrategy(String value) {
        this.value = value;
    }
	
	public static boolean isValidStrategy(String strategy) {
		return asList(values())
				.stream()
				.map(CartOptimizationStrategy::getValue)
				.anyMatch(val -> Objects.equals(val, strategy));
	}
	
	
	
	
	public static Optional<CartOptimizationStrategy> getCartOptimizationStrategy(String name) {
		return stream(values())
				.filter(strategy -> Objects.equals(strategy.getValue(), name))
				.findFirst();
	};
}
