package com.nasnav.shipping.services;

import static com.nasnav.exceptions.ErrorCodes.G$JSON$0001;
import static com.nasnav.service.model.common.ParameterType.*;
import static com.nasnav.shipping.model.ShippingServiceType.DELIVERY;
import static com.nasnav.shipping.model.ShippingServiceType.PICKUP;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import com.nasnav.shipping.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.DummyCallbackDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;

import lombok.Getter;
import org.json.JSONObject;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DummyShippingService implements ShippingService {
	
	private final Logger  logger = LogManager.getLogger(getClass());
	public static final String ID = "TEST";
	private static final String NAME = "Dummy shipping service";
	private static final String BILL_FILE= "NOT EMPTY";
	public static final int BILL_FILE_SIZE = BILL_FILE.length();
	public static final String RETURN_EMAIL_MSG = "TEST.. TEST .. YOU WILL GET NOTHING!!!!";
	public static final String SHOP_ID = "\"Shop Id\"";
	public static final String ICON = "";

	@Getter
	private List<ServiceParameter> serviceParameters;
	

	@Override
	public ShippingServiceInfo getServiceInfo() {
		List<Parameter> serviceParamerters = 
				asList( new Parameter("Hot Line", NUMBER)
						, new Parameter("Shops", STRING_ARRAY)
						, new Parameter("Optional_param", STRING, false));
		List<Parameter> additionalData = asList(new Parameter(SHOP_ID, NUMBER));
		return new ShippingServiceInfo(ID, NAME, false, serviceParamerters, additionalData, PICKUP, ICON);
	}

	
	
	
	
	@Override
	public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
		return ofNullable(items)
				.orElse(emptyList())
				.stream()
				.map(this::createFlatShipmentOffer)
				.collect(collectingAndThen( 
						toList()
						, shipments -> Mono.just(new ShippingOffer(getServiceInfo(), shipments))));
	}

	
	
	
	
	@Override
	public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
		ShipmentTracker tracker = createRandomShipmentTracker(items.get(0));
		return Flux.fromIterable(asList(tracker));
	}



	private ShipmentTracker createRandomShipmentTracker(ShippingDetails item) {
		ShipmentTracker tracker = new ShipmentTracker();
		tracker.setShipmentExternalId(randomUUID().toString());
		tracker.setTracker(randomUUID().toString());
		tracker.setAirwayBillFile("Tk9UIEVNUFRZ");
		tracker.setShippingDetails(item);
		return tracker;
	}


	@Override
	public void validateShipment(List<ShippingDetails> items) {
	}


	@Override
	public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params){
		ObjectMapper mapper = new ObjectMapper();
		DummyCallbackDTO body;
		try {
			body = mapper.readValue(params, DummyCallbackDTO.class);
		} catch (IOException e) {
			logger.error(e, e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$JSON$0001);
		}
		return new ShipmentStatusData(serviceId, orgId, body.getId(), body.getStatus(), body.getMessage());
	}



	@Override
	public Optional<Long> getPickupShop(String additionalParametersJson) {
		try{
			return ofNullable(additionalParametersJson)
					.map(JSONObject::new)
					.map(json -> json.getLong(SHOP_ID));
		}catch(Throwable e){
			logger.error(e,e);
			return Optional.empty();
		}
	}



	private ShippingEta getFlatEta() {
		return new ShippingEta(now().plusDays(1), now().plusDays(2));
	}
	
	
	
	
	private BigDecimal getFlatRate() {
		return new BigDecimal("25.5");
	}
	
	
	private Shipment createFlatShipmentOffer(ShippingDetails request) {
		return request
				.getItems()
				.stream()
				.map(ShipmentItems::getStockId)
				.collect(
					collectingAndThen(
						toList()
						, stocks -> new Shipment(getFlatRate(), getFlatEta(), stocks, request.getSubOrderId())));
	}





	@Override
	public void setServiceParameters(List<ServiceParameter> params) {
		this.serviceParameters = params;
	}



	@Override
	public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
		List<ReturnShipmentTracker> trackers =
				items
				.stream()
				.map(this::createRandomShipmentTracker)
				.map(shpTracker -> new ReturnShipmentTracker(shpTracker, RETURN_EMAIL_MSG))
				.collect(toList());
		return Flux.fromIterable(trackers);
	}

}
