package com.nasnav.shipping.services;

import static com.nasnav.shipping.model.ParameterType.LONG;
import static com.nasnav.shipping.model.ParameterType.LONG_ARRAY;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.json.JSONArray;
import org.springframework.beans.factory.annotation.Autowired;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.service.OrderService;
import com.nasnav.service.model.cart.ShopFulfillingCart;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.Parameter;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.Shipment;
import com.nasnav.shipping.model.ShipmentItems;
import com.nasnav.shipping.model.ShipmentStatusData;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShipmentValidation;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingEta;
import com.nasnav.shipping.model.ShippingOffer;
import com.nasnav.shipping.model.ShippingServiceInfo;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PickupFromShop implements ShippingService{
	
	public static final String SERVICE_ID = "PICKUP"; 
	public static final String SERVICE_NAME = "Pickup from Shop";
	public static final String ALLOWED_SHOP_ID_LIST = "ALLOWED_SHOP_ID_LIST";
	public static final String SHOP_ID = "SHOP_ID";
	
	private static List<Parameter> SERVICE_PARAM_DEFINITION = 
			asList(new Parameter(ALLOWED_SHOP_ID_LIST, LONG_ARRAY));
	
	private static List<Parameter> ADDITIONAL_PARAM_DEFINITION = 
			asList(new Parameter(SHOP_ID, LONG));
	
	private List<Long> allowedShops;

	
	
	
	@Autowired
	private OrderService orderService;
	
	
	
	public PickupFromShop() {
		allowedShops = emptyList();
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
				.collect(toList());
	}
	
	
	
	

	@Override
	public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
		Mono<ShipmentValidation> validation = validateShipment(items);
		
		ShippingServiceInfo serviceInfo = createServiceInfoWithShopsOptions();
		
		List<Shipment> shipments =
				items
				.stream()
				.map(this::createShipment)
				.collect(toList());
		
		ShippingOffer offer = new ShippingOffer(serviceInfo, shipments);
		return validation
				.flatMap(valid -> 
					valid.getIsValid()? 
							Mono.just(offer) : Mono.empty() );
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
	
	
	
	
	private ShippingServiceInfo createServiceInfoWithShopsOptions() {
		List<String> possiblePickupShops = 
				orderService
				.getShopsThatCanProvideWholeCart()
				.stream()
				.map(ShopFulfillingCart::getShopId)
				.map(id -> id.toString())
				.collect(toList());
		
		ShippingServiceInfo serviceInfo = getServiceInfo();
		
		serviceInfo
		.getAdditionalDataParams()
		.stream()
		.filter(param -> Objects.equals(param.getName(), SHOP_ID))
		.findFirst()
		.ifPresent(param -> param.setOptions(possiblePickupShops));
		
		return serviceInfo;
	}

	
	
	
	@Override
	public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
		return Flux.just(new ShipmentTracker());
	}

	
	
	
	@Override
	public Mono<ShipmentValidation> validateShipment(List<ShippingDetails> items) {
		boolean isCartFromSingleShop = items.size() == 1;
		boolean isShopAllowedForPickup = 
				items
				.stream()
				.map(ShippingDetails::getShopId)
				.allMatch(allowedShops::contains);
		
		boolean isValid = isCartFromSingleShop && isShopAllowedForPickup;
		String message = "";
		if(!isCartFromSingleShop) {
			message = "Cart has items from multiple shops, while pickup service is allowed "
					+ "for a single shop!";
		}else if(!isShopAllowedForPickup) {
			message = "Selected shop is not valid for pickup service!";
		}
		
		return Mono.just(new ShipmentValidation(isValid, message));
	}

	
	
	
	@Override
	public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
		ShipmentStatusData status = new ShipmentStatusData();
		status.setOrgId(orgId);
		status.setServiceId(serviceId);
		status.setState(ShippingStatus.valueOf(params).getValue());
		return status;
	}

}
