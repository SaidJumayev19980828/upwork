package com.nasnav.service.model.cart.optimization;

import com.nasnav.service.cart.optimizers.CartOptimizationStrategy;
import com.nasnav.service.model.common.Parameter;
import lombok.Data;

import java.util.List;

@Data
public class CartOptimizationStrategyInfo {
	private CartOptimizationStrategy strategy;
	private List<Parameter> parametersDefinition;
}
