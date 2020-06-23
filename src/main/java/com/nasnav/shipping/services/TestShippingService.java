package com.nasnav.shipping.services;

import static com.nasnav.shipping.model.ParameterType.LONG;
import static com.nasnav.shipping.model.ParameterType.STRING;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;

import java.math.BigDecimal;
import java.util.List;

import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;

import lombok.Getter;
import reactor.core.publisher.Mono;

public class TestShippingService implements ShippingService {
	
	
	private static final String ID = "TEST";
	private static final String NAME = "Dummy shipping service";
	public static final int BILL_FILE_SIZE = 100;
	
	@Getter
	private List<ServiceParameter> serviceParameters;
	

	@Override
	public ShippingServiceInfo getServiceInfo() {
		List<Parameter> serviceParamerters = asList( new Parameter("Hot Line", STRING));
		List<Parameter> additionalData = asList(new Parameter("Shop Id", LONG));
		return new ShippingServiceInfo(ID, NAME, serviceParamerters, additionalData);
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
	public Mono<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
		ShipmentTracker tracker = new ShipmentTracker();
		tracker.setShipmentExternalId(randomUUID().toString());
		tracker.setTracker(randomUUID().toString());
		tracker.setAirwayBillFile(new Byte[BILL_FILE_SIZE]);
		return Mono.just(tracker);
	}

	@Override
	public Mono<ShipmentValidation> validateShipment(List<ShippingDetails> items) {
		return Mono.empty();
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
						, stocks -> new Shipment(getFlatRate(), getFlatEta(), stocks)));
	}





	@Override
	public void setServiceParameters(List<ServiceParameter> params) {
		this.serviceParameters = params;
	}
	

}
