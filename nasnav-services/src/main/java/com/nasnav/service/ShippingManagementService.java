package com.nasnav.service;

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ShippingManagementService {

	List<ShippingOfferDTO> getShippingOffers(Long customerAddress);
	void registerToShippingService(ShippingServiceRegistration registration);
	void unregisterFromShippingService(String serviceId);
	void validateCartForShipping(List<CartCheckoutData> cartItemData, CartCheckoutDTO dto);
	Optional<ShippingService> getShippingService(OrganizationShippingServiceEntity orgShippingService);
	List<ShippingOfferDTO> getOffersFromOrganizationShippingServices(List<ShippingDetails> shippingDetails);
	public List<ServiceParameter> parseServiceParameters(OrganizationShippingServiceEntity orgShippingService);
	Mono<ShipmentTracker> requestShipment(OrdersEntity subOrder);
	ShippingDetails createShippingDetailsFromOrder(OrdersEntity subOrder);
	ShippingDetails createShippingDetailsFromOrder(OrdersEntity subOrder, Map<String,String> additionalParameters);
	void updateShipmentStatus(String serviceId, Long orgId, String params) throws IOException;
	List<ShippingServiceRegistration> listShippingServices();
	Optional<String> getShippingServiceCartOptimizer(String shippingServiceId);
	Flux<ReturnShipmentTracker> requestReturnShipments(ReturnRequestEntity returnRequest);
	Optional<ShopsEntity> getPickupShop(String additionalDataJson, String shippingServiceId, Long orgId);
	Optional<ShopsEntity> getPickupShop(ShipmentEntity shipment);
	boolean isPickupService(String shippingServiceId);
	Optional<ShippingServiceInfo> getShippingServiceInfo(String shippingServiceId);
	OrderConfirmResponseDTO getShippingAirwayBill(Long orderId);
	String getTrackingUrl(Long orderId);
}