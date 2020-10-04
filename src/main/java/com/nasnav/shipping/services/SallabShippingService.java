package com.nasnav.shipping.services;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.ShippingStatus.DELIVERED;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0002;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0010;
import static com.nasnav.service.model.common.ParameterType.JSON;
import static com.nasnav.service.model.common.ParameterType.LONG_ARRAY;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.collectingAndThen;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.ShipmentRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.ReturnShipmentTracker;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.Shipment;
import com.nasnav.shipping.model.ShipmentItems;
import com.nasnav.shipping.model.ShipmentStatusData;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingEta;
import com.nasnav.shipping.model.ShippingOffer;
import com.nasnav.shipping.model.ShippingServiceInfo;

import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class SallabShippingService implements ShippingService{

	Logger logger = LogManager.getLogger();
	
	static final public String TIERES = "TIERS";
	static final public String SERVICE_ID = "SALLAB_PRICE_PRECENTAGE";
	static final public String SERVICE_NAME = "Special Shipping";
	static final public String SUPPORTED_CITIES = "SUPPORTED_CITIES";
	private static final String RETURN_SHIPMENT_EMAIL_MSG = "Please call customer service to arrange a return shipment, and sorry again for any inconvenience!";
	
	
	private static List<Parameter> SERVICE_PARAM_DEFINITION = 
			asList(new Parameter(TIERES, JSON), new Parameter(SUPPORTED_CITIES , LONG_ARRAY));

	private ShippingTiers tiers;
	private List<Long> supportedCities;
	
	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private ShipmentRepository shipmentRepo;

	
	
	
	@Override
	public ShippingServiceInfo getServiceInfo() {
		return new ShippingServiceInfo(SERVICE_ID, SERVICE_NAME, false, SERVICE_PARAM_DEFINITION, emptyList());
	}

	
	
	

	@Override
	public void setServiceParameters(List<ServiceParameter> params) {
		String tiersJsonString = 
				params
				.stream()
				.filter(param -> Objects.equals(param.getParameter(), TIERES))
				.map(ServiceParameter::getValue)
				.findFirst()
				.orElse("{}");
		String supportedCitiesString = 
				params
				.stream()
				.filter(param -> Objects.equals(param.getParameter(), SUPPORTED_CITIES))
				.map(ServiceParameter::getValue)
				.findFirst()
				.orElse("[]");
				
		try {
			tiers = objectMapper.readValue(tiersJsonString, ShippingTiers.class);
			supportedCities = objectMapper.readValue(supportedCitiesString, new TypeReference<List<Long>>(){});
			validateServiceParameters(tiers);
			validateSupportedCities(supportedCities);
		} catch (IOException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
		}
	}





	private void validateSupportedCities(List<Long> cities) {
		if(cities == null || cities.isEmpty()) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
		}
	}





	private void validateServiceParameters( ShippingTiers tiers) {
		List<Tier> tiersList = tiers.getTiers();
		if( tiersList == null || tiersList.isEmpty()) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
		}
		tiersList.forEach(this::validateTierData);
	}
	
	
	
	
	
	private void validateTierData(Tier tier) {
		if(anyIsNull(tier.getStartInclusive(), tier.getEndExclusive(), tier.getPercentage())) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
		}
	}

	
	

	@Override
	public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> shippingInfo) {
		BigDecimal itemTotalValue = calcItemsTotalValue(shippingInfo);
		Optional<BigDecimal> shippingFeePercentage = getShippingFeePercentage(itemTotalValue);
		
		if(!shippingFeePercentage.isPresent() ||  !areCitiesSupported(shippingInfo)) {
			return Mono.empty();
		}
		return shippingInfo
				.stream()
				.map(subOrderInfo -> createShipmentOfferForSubOrder(subOrderInfo, shippingFeePercentage.get()))
				.collect(
						collectingAndThen(
								toList()
								, shipments -> Mono.just(new ShippingOffer(getServiceInfo(), shipments))));
	}
	
	
	
	
	private Shipment createShipmentOfferForSubOrder(ShippingDetails shippingInfo, BigDecimal feePercentage) {
		
		BigDecimal fee = calcFeeForOrder(shippingInfo, feePercentage);
		ShippingEta eta = new ShippingEta(now().plusDays(1L), now().plusDays(3L));
		List<Long> stockIds = 
				shippingInfo
				.getItems()
				.stream()
				.map(ShipmentItems::getStockId)
				.collect(toList());
		return new Shipment(fee, eta, stockIds, shippingInfo.getSubOrderId());
	}


	
	
	
	private BigDecimal calcFeeForOrder(ShippingDetails shippingInfo, BigDecimal feePercentage) {
		BigDecimal itemsValue  = 
				shippingInfo
				.getItems()
				.stream()
				.map(this::getItemValue)
				.reduce(ZERO, BigDecimal::add);
		return doCalcFee(feePercentage, itemsValue);
	}





	private Optional<BigDecimal> getShippingFeePercentage(BigDecimal itemTotalValue) {
		return tiers
				.getTiers()
				.stream()
				.filter(tier -> isValueInTier(tier, itemTotalValue))
				.map(this::getTierPercentage)
				.findFirst();
	}

	
	
	
	private BigDecimal getTierPercentage(Tier tier) {
		Integer maxFreeShipments = ofNullable(tier.getMaxFreeShipments()).orElse(0);
		Optional<BaseUserEntity> user = securityService.getCurrentUserOptional();
		if(maxFreeShipments == 0) {
			return tier.getPercentage();
		}

		Integer perviousFreeOrdersNum =
				user
				.map(u -> shipmentRepo.countFreeShipments(u.getId(), SERVICE_ID))
				.orElse(0);
		if(perviousFreeOrdersNum < maxFreeShipments) {
			return ZERO;
		}
		return tier.getPercentage();
	}

	
	
	
	private BigDecimal doCalcFee(BigDecimal percentage, BigDecimal val) {
		return percentage
				.multiply(new BigDecimal("0.01"))
				.multiply(val)
				.setScale(2, HALF_EVEN);
	}
	
	
	
	private boolean isValueInTier(Tier tier, BigDecimal value) {
		BigDecimal startInclusive = tier.getStartInclusive();
		BigDecimal endExclusive = tier.getEndExclusive();
		return value.compareTo(startInclusive) >= 0
				&& value.compareTo(endExclusive) < 0 ;
	}



	private BigDecimal calcItemsTotalValue(List<ShippingDetails> items) {
		return items
				.stream()
				.map(ShippingDetails::getItems)
				.flatMap(List::stream)
				.map(this::getItemValue)
				.reduce(ZERO, BigDecimal::add)
				.setScale(2, HALF_EVEN);
	}



	private BigDecimal getItemValue(ShipmentItems shipmentItems) {
		BigDecimal price = shipmentItems.getPrice();
		return price.multiply(BigDecimal.valueOf(shipmentItems.getQuantity()));
	}




	@Override
	public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
		validateShipment(items);
		return items
				.stream()
				.map(shippingDetails -> new ShipmentTracker(null, null, null, shippingDetails))
				.collect(
						collectingAndThen(
								toList()
								, Flux::fromIterable));
	}

	
	

	@Override
	public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
		validateShipment(items);
		return items
				.stream()
				.map(this::createReturnShipment)
				.collect(
						collectingAndThen(
								toList()
								, Flux::fromIterable));
		
	}

	
	
	
	private ReturnShipmentTracker createReturnShipment(ShippingDetails shippingDetails) {
		ReturnShipmentTracker tracker = new ReturnShipmentTracker();
		tracker.setAirwayBillFile(null);
		tracker.setShipmentExternalId(null);
		tracker.setShippingDetails(shippingDetails);
		tracker.setTracker(null);
		tracker.setEmailMessage(RETURN_SHIPMENT_EMAIL_MSG);
		return tracker;
	}
	
	

	@Override
	public void validateShipment(List<ShippingDetails> details) {
		boolean citiesAreSupported = areCitiesSupported(details);
		if(!citiesAreSupported) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0010);
		}
	}





	private boolean areCitiesSupported(List<ShippingDetails> details) {
		return details
		.stream()
		.map(this::getCities)
		.flatMap(List::stream)
		.allMatch(supportedCities::contains);
	}

	
	
	private List<Long> getCities(ShippingDetails details){
		Long desitinationCity = details.getDestination().getCity();
		Long sourceCity = details.getSource().getCity();
		return asList(desitinationCity, sourceCity);
	}
	
	

	@Override
	public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params) {
		ShipmentStatusData statusData = new ShipmentStatusData();
		statusData.setExternalShipmentId(null);
		statusData.setOrgId(orgId);
		statusData.setServiceId(serviceId);
		statusData.setState(DELIVERED.getValue());
		return statusData;
	}

	
	
}



@Data
class ShippingTiers{
	private List<Tier> tiers;
}



@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
class Tier{
	private BigDecimal startInclusive;
	private BigDecimal endExclusive;
	private BigDecimal percentage;
	private Integer maxFreeShipments;
}
