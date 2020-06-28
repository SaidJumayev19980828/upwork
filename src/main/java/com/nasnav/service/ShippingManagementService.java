package com.nasnav.service;

import java.util.List;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;

public interface ShippingManagementService {

	List<ShippingOfferDTO> getShippingOffers(Long customerAddress);
	void registerToShippingService(ShippingServiceRegistration registration);
}