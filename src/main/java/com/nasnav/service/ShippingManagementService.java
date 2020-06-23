package com.nasnav.service;

import java.util.List;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;

public interface ShippingManagementService {

	List<ShippingOfferDTO> getShippingOffers(Long customerAddress);
	void validateShippingAdditionalData(CartCheckoutDTO dto);
}