package com.nasnav.shipping.services;

import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0011;
import static com.nasnav.service.model.common.ParameterType.LONG_ARRAY;
import static com.nasnav.service.model.common.ParameterType.NUMBER;
import static java.lang.Long.MIN_VALUE;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.StreamSupport;

import com.nasnav.shipping.model.*;
import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.OrderService;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PickupFromShop implements ShippingService{
	
	public static final String SERVICE_ID = "PICKUP"; 
	public static final String SERVICE_NAME = "Pickup from Shop";
	public static final String ALLOWED_SHOP_ID_LIST = "ALLOWED_SHOP_ID_LIST";
	public static final String SHOP_ID = "SHOP_ID";
	public static final String RETURN_EMAIL_MSG =
			"Thanks for you patience! To complete the return process, please return " +
			" the items back to shop and provide the sellers with this email!";

	private static List<Parameter> SERVICE_PARAM_DEFINITION = 
			asList(new Parameter(ALLOWED_SHOP_ID_LIST, LONG_ARRAY));
	
	private static List<Parameter> ADDITIONAL_PARAM_DEFINITION = 
			asList(new Parameter(SHOP_ID, NUMBER));
	
	private Set<Long> allowedShops;

	
	
	
	@Autowired
	private OrderService orderService;
	
	
	
	public PickupFromShop() {
		allowedShops = emptySet();
	}
	
	
	
	@Override
	public ShippingServiceInfo getServiceInfo() {
		return new ShippingServiceInfo(SERVICE_ID, SERVICE_NAME, true
				, SERVICE_PARAM_DEFINITION, ADDITIONAL_PARAM_DEFINITION);
	}

	
	
	
	@Override
	public void setServiceParameters(List<ServiceParameter> params) {
		allowedShops = 
				params
				.stream()
				.filter(param -> Objects.equals(param.getParameter(), ALLOWED_SHOP_ID_LIST))
				.map(ServiceParameter::getValue)
				.map(JSONArray::new)
				.map(JSONArray::spliterator)
				.flatMap(iterator -> StreamSupport.stream(iterator, false))
				.map(Object::toString)
				.map(EntityUtils::parseLongSafely)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toSet());
	}
	
	
	
	

	@Override
	public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
		List<String> possiblePickupShops = getShopsThatCanProvideWholeCart();
		
		ShippingServiceInfo serviceInfo = createServiceInfoWithShopsOptions(possiblePickupShops);
		
		List<Shipment> shipments =
				items
				.stream()
				.map(this::createShipment)
				.collect(toList());
		
		ShippingOffer offer = new ShippingOffer(serviceInfo, shipments);
		return possiblePickupShops.isEmpty()? 
				Mono.empty() : Mono.just(offer) ;
	}



	
	
	private Shipment createShipment(ShippingDetails shippingDetails) {
		BigDecimal fee = BigDecimal.ZERO;
		ShippingEta eta = new ShippingEta(now().plusDays(1), now().plusDays(7));
		List<Long> stocks = 
				shippingDetails
				.getItems()
				.stream()
				.map(ShipmentItems::getStockId)
				.collect(toList());
		Long orderId = shippingDetails.getSubOrderId();
		return new Shipment(fee, eta, stocks ,orderId);
	}
	
	
	
	
	private ShippingServiceInfo createServiceInfoWithShopsOptions(List<String> possiblePickupShops) {
		ShippingServiceInfo serviceInfo = getServiceInfo();
		
		serviceInfo
		.getAdditionalDataParams()
		.stream()
		.filter(param -> Objects.equals(param.getName(), SHOP_ID))
		.findFirst()
		.ifPresent(param -> param.setOptions(possiblePickupShops));
		
		return serviceInfo;
	}



	private List<String> getShopsThatCanProvideWholeCart() {
		return orderService
				.getShopsThatCanProvideWholeCart()
				.stream()
				.map(ShopFulfillingCart::getShopId)
				.filter(allowedShops::contains)
				.map(id -> id.toString())
				.collect(toList());
	}

	
	
	
	@Override
	public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {

		return Flux.just(new ShipmentTracker());
	}



	@Override
	public void validateShipment(List<ShippingDetails> items) {
		boolean isCartFromSingleShop = items.size() == 1;
		boolean isShopAllowedForPickup = isShopAllowedForPickup(items);
		
		String message = "";
		if(!isCartFromSingleShop) {
			message = "Cart has items from multiple shops, while pickup service is allowed "
					+ "for a single shop!";
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0011, message);
		}else if(!isShopAllowedForPickup) {
			message = "Selected shop is not valid for pickup service!";
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0011, message);
		}
		
	}



	private boolean isShopAllowedForPickup(List<ShippingDetails> items) {
		return items
				.stream()
				.map(ShippingDetails::getAdditionalData)
				.map(data -> data.get(SHOP_ID))
				.map(EntityUtils::parseLongSafely)
				.map(id -> id.orElse(MIN_VALUE))
				.allMatch(allowedShops::contains);
	}

	
	
	
	@Override
	public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
		ShipmentStatusData status = new ShipmentStatusData();
		status.setOrgId(orgId);
		status.setServiceId(serviceId);
		status.setState(ShippingStatus.valueOf(params).getValue());
		return status;
	}




	@Override
	public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
		return requestShipment(items)
				.map(shpTracker -> new ReturnShipmentTracker(shpTracker, RETURN_EMAIL_MSG));
	}

}
