package com.nasnav.shipping;

import java.util.List;

import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.ShipmentStatusData;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingOffer;
import com.nasnav.shipping.model.ShippingServiceInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface ShippingService {
	
	ShippingServiceInfo getServiceInfo();
	
	void setServiceParameters(List<ServiceParameter> params);
	
	/**
	 * @param items items to shipped are grouped by the pickup address, several pickup addresses can be provided, and
	 * the shipping service may offer to cover them by multiple shipments. 
	 * @return a shipping service offer. the offer may suggest several shipments to do the service, in case the
	 * items will be picked up from several locations.
	 * The offer should return the shipments fee, estimated time of arrival as well.
	 * If the shipping service can't provide a service for the provided addresses, it should return Mono.empty().
	 * */
	Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items);
	
	
	/**
	 * call the shipping services api's if needed to request a shipping service.
	 * @param items items to shipped are grouped by the pickup address, several pickup addresses can be provided, and
	 * the shipping service may offer to cover them by multiple shipments. 
	 * @return shipment tracking information, including the airway bill.
	 * */
	Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items);

	void validateShipment(List<ShippingDetails> items);

	ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params);
}
