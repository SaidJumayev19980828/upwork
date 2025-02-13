package com.nasnav.shipping;

import com.nasnav.shipping.model.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

public interface ShippingService {
	
	ShippingServiceInfo getServiceInfo();
	
	void setServiceParameters(List<ServiceParameter> params);
	
	/**
	 * @param items items to shipped are grouped by the pickup address, several pickup addresses can be provided, and
	 * the shipping service may offer to cover them by multiple shipments. 
	 * @return a shipping service offer. the offer may suggest several shipments to do the service, in case the
	 * items will be picked up from several locations.
	 * The offer should return the shipments fee, estimated time of arrival as well.
	 * If the shipping service can't provide a service for the provided addresses, it may either :
	 * - return Mono.empty(), in this case, it wont be returned in GET /shipping/offers response
	 * - return an offer with ShippingOffer.available flag = false, and provide a message in ShippingOffer.message
	 * */
	Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items);
	
	
	/**
	 * call the shipping services api's if needed to request a shipping service.
	 * @param items items to shipped are grouped by the pickup address, several pickup addresses can be provided, and
	 * the shipping service may offer to cover them by multiple shipments. 
	 * @return shipment tracking information, including the airway bill.
	 * */
	Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items);

	/**
	 * call the shipping services api's if needed to request a return shipping service.
	 * a return shipment is a shipment that returns items from the customer back to shops.
	 * Some shipping services may not provide return shipments at the first place, like pickup
	 * services, or may have different logic for creating return shipments.
	 * That's why it has its own api.
	 * @param items items to shipped are grouped by the pickup address, several pickup addresses can be provided, and
	 * the shipping service may offer to cover them by multiple shipments.
	 * @return shipment tracking information, including the airway bill.
	 * */
	Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items);

	void validateShipment(List<ShippingDetails> items);

	ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params);

	Optional<Long> getPickupShop(String additionalParametersJson);

	Mono<String> getAirwayBill(String airwayBillNumber);

	String getTrackingUrl(String trackingNumber);
}
