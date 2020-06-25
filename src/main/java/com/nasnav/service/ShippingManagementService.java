package com.nasnav.service;

import java.math.BigDecimal;
import java.util.List;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingEta;

public interface ShippingManagementService {

	List<ShippingOfferDTO> getShippingOffers(Long customerAddress);
	void validateShippingAdditionalData(CartCheckoutDTO dto);

	List<ShippingOfferDTO> getOffersFromOrganizationShippingServices(List<ShippingDetails> shippingDetails);
}