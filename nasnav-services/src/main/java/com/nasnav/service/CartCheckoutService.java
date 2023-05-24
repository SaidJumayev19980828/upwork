package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.response.navbox.Order;

public interface CartCheckoutService {

	Order checkoutCart(CartCheckoutDTO dto);

	Order checkoutYeshteryCart(CartCheckoutDTO dto);

}