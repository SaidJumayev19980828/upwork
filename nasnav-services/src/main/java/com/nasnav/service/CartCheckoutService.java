package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.cart.StoreCheckoutDto;
import com.nasnav.dto.response.navbox.Order;
import com.nasnav.exceptions.BusinessException;

public interface CartCheckoutService {

	void initiateCheckout(Long userId);

	Order checkoutCart(CartCheckoutDTO dto);

	Order completeCheckout(CartCheckoutDTO dto) throws BusinessException;

	Order completeYeshteryCheckout(CartCheckoutDTO dto) throws BusinessException;

	Order checkoutYeshteryCart(CartCheckoutDTO dto);

	StoreCheckoutDto storeCheckout(Long userId);
}