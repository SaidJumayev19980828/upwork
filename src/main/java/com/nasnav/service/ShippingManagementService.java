package com.nasnav.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationShippingServiceEntity;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShippingDetails;

import reactor.core.publisher.Mono;

public interface ShippingManagementService {

	List<ShippingOfferDTO> getShippingOffers(Long customerAddress);
	void registerToShippingService(ShippingServiceRegistration registration);
	void validateShippingAdditionalData(CartCheckoutDTO dto);
	Optional<ShippingService> getShippingService(OrganizationShippingServiceEntity orgShippingService);
	List<ShippingOfferDTO> getOffersFromOrganizationShippingServices(List<ShippingDetails> shippingDetails);
	public List<ServiceParameter> parseServiceParameters(OrganizationShippingServiceEntity orgShippingService);
	Mono<ShipmentTracker> requestShipment(OrdersEntity subOrder);
	ShippingDetails createShippingDetailsFromOrder(OrdersEntity subOrder);
	ShippingDetails createShippingDetailsFromOrder(OrdersEntity subOrder, Map<String,String> additionalParameters);
}