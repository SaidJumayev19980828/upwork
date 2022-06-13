package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;

import java.util.List;

public interface CartOptimizationService {
	<T> void setCartOptimizationStrategy(CartOptimizationSettingDTO settingDto);
	CartOptimizeResponseDTO optimizeCart(CartCheckoutDTO dto);

	CartOptimizeResponseDTO validateAndOptimizeCart(CartCheckoutDTO dto);
	List<CartOptimizationSettingDTO> getCartOptimizationStrategy();
	List<CartOptimizationStrategyDTO> listAllCartOptimizationStrategies();
    void deleteCartOptimizationStrategy(String strategyName, String shippingService);
}
