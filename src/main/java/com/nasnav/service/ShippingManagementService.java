package com.nasnav.service;

import java.util.List;
import java.util.Optional;

import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationShippingServiceEntity;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.ShipmentTracker;

import reactor.core.publisher.Mono;

public interface ShippingManagementService {

	List<ShippingOfferDTO> getShippingOffers(Long customerAddress);
	void registerToShippingService(ShippingServiceRegistration registration);
	Optional<ShippingService> getShippingService(OrganizationShippingServiceEntity orgShippingService);
	Mono<ShipmentTracker> requestShipment(OrdersEntity subOrder);
}