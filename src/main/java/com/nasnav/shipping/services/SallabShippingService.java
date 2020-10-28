package com.nasnav.shipping.services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.ShipmentRepository;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.enumerations.ShippingStatus.DELIVERED;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0002;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0010;
import static com.nasnav.service.model.common.ParameterType.*;
import static com.nasnav.service.model.common.ParameterType.NUMBER;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.FLOOR;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public class SallabShippingService implements ShippingService{

	Logger logger = LogManager.getLogger();
	
	static final public String TIERS = "TIERS";
	static final public String SERVICE_ID = "SALLAB_PRICE_PRECENTAGE";
	static final public String SERVICE_NAME = "Special Shipping";
	static final public String SUPPORTED_CITIES = "SUPPORTED_CITIES";
	static final public String MIN_SHIPPING_FEE = "MIN_SHIPPING_FEE";
	private static final String RETURN_SHIPMENT_EMAIL_MSG = "Please call customer service to arrange a return shipment, and sorry again for any inconvenience!";
	public static final String ETA_DAYS_MIN = "ETA_DAYS_MIN";
	public static final String ETA_DAYS_MAX = "ETA_DAYS_MAX";

	private static final Integer ETA_DAYS_MIN_DEFAULT = 1;
	private static final Integer ETA_DAYS_MAX_DEFAULT = 3;
	
	private static final List<Parameter> SERVICE_PARAM_DEFINITION =
			asList(new Parameter(TIERS, JSON)
					, new Parameter(SUPPORTED_CITIES , LONG_ARRAY)
					, new Parameter(MIN_SHIPPING_FEE, NUMBER)
					, new Parameter(ETA_DAYS_MIN, NUMBER, false)
					, new Parameter(ETA_DAYS_MAX, NUMBER, false));

	private ShippingTiers tiers;
	private List<Long> supportedCities;
	private BigDecimal minFee;
	private Integer etaDaysMin;
	private Integer etaDaysMax;

	@Autowired
	private ObjectMapper objectMapper;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private ShipmentRepository shipmentRepo;



	public SallabShippingService() {
		etaDaysMin = ETA_DAYS_MIN_DEFAULT;
		etaDaysMax = ETA_DAYS_MAX_DEFAULT;
	}
	
	
	@Override
	public ShippingServiceInfo getServiceInfo() {
		return new ShippingServiceInfo(SERVICE_ID, SERVICE_NAME, false, SERVICE_PARAM_DEFINITION, emptyList());
	}

	
	
	

	@Override
	public void setServiceParameters(List<ServiceParameter> params) {
		Map<String, String> serviceParameters = params
				.stream()
				.collect(
					toMap(ServiceParameter::getParameter, ServiceParameter::getValue));

		String tiersJsonString =
				ofNullable(serviceParameters.get(TIERS))
				.orElse("{}");

		String supportedCitiesString =
				ofNullable(serviceParameters.get(SUPPORTED_CITIES))
				.orElse("[]");

		String minFeeString =
				ofNullable(serviceParameters.get(MIN_SHIPPING_FEE))
						.orElse("0");
		try {
			tiers = objectMapper.readValue(tiersJsonString, ShippingTiers.class);
			supportedCities = objectMapper.readValue(supportedCitiesString, new TypeReference<List<Long>>(){});
			minFee = new BigDecimal(minFeeString);
			validateServiceParameters(tiers);
			validateSupportedCities(supportedCities);
			setEtaDaysMin(serviceParameters);
			setEtaDaysMax(serviceParameters);
		} catch (Throwable e) {
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

		Optional<Fee> fee = calcFeeData(shippingInfo);
		
		if(!fee.isPresent() ||  !areCitiesSupported(shippingInfo)) {
			return Mono.empty();
		}

		Integer shipmentsNum = shippingInfo.size();
		List<Shipment> shipments =
				shippingInfo
				.stream()
				.map(subOrderInfo -> createShipmentOfferForSubOrder(subOrderInfo, fee.get(), shipmentsNum))
				.collect(toList());

		correctCalculationError(fee.get(), shipments);

		return Mono.just(new ShippingOffer(getServiceInfo(), shipments));
	}



	private void correctCalculationError(Fee fee, List<Shipment> shipments) {
		BigDecimal accumlatedFeeTotal =
				shipments
				.stream()
				.map(Shipment::getShippingFee)
				.reduce(ZERO, BigDecimal::add);
		BigDecimal error = fee.getTotalFee().subtract(accumlatedFeeTotal);
		shipments
			.stream()
			.peek( shipment -> shipment.setShippingFee(shipment.getShippingFee().add(error)))
			.findFirst();
	}



	private Optional<Fee> calcFeeData(List<ShippingDetails> shippingInfo) {
		BigDecimal itemTotalValue = calcItemsTotalValue(shippingInfo);
		return getTier(itemTotalValue)
				.map(tier -> createFee(tier,itemTotalValue));
	}



	private Fee createFee(Tier tier, BigDecimal itemTotalValue){
		boolean isFree = isFreeShipment(tier);
		BigDecimal totalFee = calcTotalFee(tier, itemTotalValue, isFree);
		return new Fee(totalFee, tier, isFree);
	}





	BigDecimal calcTotalFee(Tier tier, BigDecimal itemsVal, boolean isFree){
		BigDecimal percentage = tier.getPercentage();
		BigDecimal totalFee = doCalcFee(percentage, itemsVal);
		if(isBelowMinShippngFee(totalFee)){
			totalFee = minFee;
		}
		if(isFree) {
			totalFee = ZERO;
		}
		return totalFee;
	}




	private boolean isBelowMinShippngFee(BigDecimal totalFee) {
		return totalFee.compareTo(minFee) <= 0;
	}



	private BigDecimal calcMinFeePerOrder(Integer shipmentsNumInt) {
		BigDecimal shipmentsNum = BigDecimal.valueOf(shipmentsNumInt);
		return minFee.divide(shipmentsNum, 2, FLOOR);
	}




	private Shipment createShipmentOfferForSubOrder(ShippingDetails shippingInfo, Fee feeData, Integer shipmentsNum) {
		BigDecimal fee = calcFeeForOrder(shippingInfo, feeData, shipmentsNum);
		ShippingEta eta = new ShippingEta(now().plusDays(etaDaysMin), now().plusDays(etaDaysMax));
		List<Long> stockIds = getItemsStockId(shippingInfo);
		return new Shipment(fee, eta, stockIds, shippingInfo.getSubOrderId());
	}



	private void setEtaDaysMax(Map<String, String> serviceParams) {
		ofNullable(serviceParams.get(ETA_DAYS_MAX))
				.flatMap(EntityUtils::parseLongSafely)
				.map(Long::intValue)
				.ifPresent(val -> etaDaysMax = val);
	}



	private void setEtaDaysMin(Map<String, String> serviceParams) {
		ofNullable(serviceParams.get(ETA_DAYS_MIN))
				.flatMap(EntityUtils::parseLongSafely)
				.map(Long::intValue)
				.ifPresent(val -> etaDaysMin = val);
	}


	private List<Long> getItemsStockId(ShippingDetails shippingInfo) {
		return shippingInfo
				.getItems()
				.stream()
				.map(ShipmentItems::getStockId)
				.collect(toList());
	}




	private BigDecimal calcFeeForOrder(ShippingDetails shippingInfo, Fee fee, Integer shipmentsNum) {
		if(fee.isFree()){
			return ZERO;
		}else if(isBelowMinShippngFee(fee.getTotalFee())){
			return calcMinFeePerOrder(shipmentsNum);
		}else{
			BigDecimal feePercentage = fee.getShippingFeeTier().getPercentage();
			BigDecimal itemsValue  = getItemsValueOfShipment(shippingInfo);
			return doCalcFee(feePercentage, itemsValue);
		}
	}



	private BigDecimal getItemsValueOfShipment(ShippingDetails shippingInfo) {
		return shippingInfo
				.getItems()
				.stream()
				.map(this::getItemValue)
				.reduce(ZERO, BigDecimal::add);
	}



	private Optional<Tier> getTier(BigDecimal itemTotalValue) {
		return tiers
				.getTiers()
				.stream()
				.filter(tier -> isValueInTier(tier, itemTotalValue))
				.findFirst();
	}

	
	



	private boolean isFreeShipment(Tier tier) {
		Integer maxFreeShipments = ofNullable(tier.getMaxFreeShipments()).orElse(0);
		Optional<BaseUserEntity> user = securityService.getCurrentUserOptional();
		if(maxFreeShipments == 0) {
			return false;
		}
		Integer previousFreeOrdersNum =
				user
				.map(u -> shipmentRepo.countFreeShipments(u.getId(), SERVICE_ID))
				.orElse(0);
		return previousFreeOrdersNum < maxFreeShipments;
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
		return new ReturnShipmentTracker(new ShipmentTracker(shippingDetails), RETURN_SHIPMENT_EMAIL_MSG);
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
		Long destinationCity = details.getDestination().getCity();
		Long sourceCity = details.getSource().getCity();
		return asList(destinationCity, sourceCity);
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



@Data
@AllArgsConstructor
class Fee{
	private BigDecimal totalFee;
	private Tier shippingFeeTier;
	private boolean free;
}