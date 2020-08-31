package com.nasnav.service;

import java.util.List;
import java.util.Optional;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.organization.CartOptimizationSettingDTO;
import com.nasnav.dto.response.CartOptimizationStrategyDTO;
import com.nasnav.dto.response.navbox.CartOptimizeResponseDTO;

public interface CartOptimizationService {
	public <T> void setCartOptimizationStrategy(CartOptimizationSettingDTO settingDto);
	public Optional<String> getCartOptimizationStrategyForOrganization();
	public CartOptimizeResponseDTO optimizeCart(CartCheckoutDTO dto);
	public List<CartOptimizationSettingDTO> getCartOptimizationStrategy();
	public List<CartOptimizationStrategyDTO> listAllCartOptimizationStrategies();
}
