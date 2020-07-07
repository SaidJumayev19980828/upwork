package com.nasnav.shipping.services;

import static com.nasnav.exceptions.ErrorCodes.G$JSON$0001;
import static com.nasnav.shipping.model.ParameterType.LONG;
import static com.nasnav.shipping.model.ParameterType.STRING;
import static com.nasnav.shipping.model.ParameterType.STRING_ARRAY;
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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dto.DummyCallbackDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
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

import lombok.Getter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class DummyShippingService implements ShippingService {
	
	private final Logger  logger = LogManager.getLogger(getClass());
	public static final String ID = "TEST";
	private static final String NAME = "Dummy shipping service";
	private static final String BILL_FILE= "NOT EMPTY";
	public static final int BILL_FILE_SIZE = BILL_FILE.length();
	
	@Getter
	private List<ServiceParameter> serviceParameters;
	

	@Override
	public ShippingServiceInfo getServiceInfo() {
		List<Parameter> serviceParamerters = 
				asList( new Parameter("Hot Line", STRING), new Parameter("Shops", STRING_ARRAY));
		List<Parameter> additionalData = asList(new Parameter("Shop Id", LONG));
		return new ShippingServiceInfo(ID, NAME, false, serviceParamerters, additionalData);
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
		ShipmentTracker tracker = new ShipmentTracker();
		tracker.setShipmentExternalId(randomUUID().toString());
		tracker.setTracker(randomUUID().toString());
		tracker.setAirwayBillFile("NOT EMPTY");
		return Flux.fromIterable(asList(tracker));
	}

	@Override
	public Mono<ShipmentValidation> validateShipment(List<ShippingDetails> items) {
		return Mono.empty();
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
	

}
