package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;

import java.util.List;

public interface CartOptimizationService {
	public <T> void setCartOptimizationStrategy(CartOptimizationSettingDTO settingDto);
	public CartOptimizeResponseDTO optimizeCart(CartCheckoutDTO dto);
	public List<CartOptimizationSettingDTO> getCartOptimizationStrategy();
	public List<CartOptimizationStrategyDTO> listAllCartOptimizationStrategies();
    void deleteCartOptimizationStrategy(String strategyName, String shippingService);
}
