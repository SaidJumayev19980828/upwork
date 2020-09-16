package com.nasnav.shipping.services.bosta;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.StringUtils.parseLongWithDefault;
import static com.nasnav.enumerations.ShippingStatus.DELIVERED;
import static com.nasnav.enumerations.ShippingStatus.EN_ROUTE;
import static com.nasnav.enumerations.ShippingStatus.FAILED;
import static com.nasnav.enumerations.ShippingStatus.PICKED_UP;
import static com.nasnav.exceptions.ErrorCodes.G$JSON$0001;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0001;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0002;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0003;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0004;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0005;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0010;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0012;
import static com.nasnav.service.model.common.ParameterType.NUMBER;
import static com.nasnav.service.model.common.ParameterType.STRING;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.math.RoundingMode.HALF_EVEN;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.apache.commons.lang3.StringUtils.leftPad;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static reactor.core.publisher.Mono.error;
import static reactor.core.publisher.Mono.just;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.Period;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.IntStream;

import com.nasnav.shipping.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.nasnav.commons.model.IndexedData;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.services.bosta.webclient.BostaWebClient;
import com.nasnav.shipping.services.bosta.webclient.dto.Address;
import com.nasnav.shipping.services.bosta.webclient.dto.BostaCallbackDTO;
import com.nasnav.shipping.services.bosta.webclient.dto.CreateAwbResponse;
import com.nasnav.shipping.services.bosta.webclient.dto.CreateDeliveryResponse;
import com.nasnav.shipping.services.bosta.webclient.dto.Delivery;
import com.nasnav.shipping.services.bosta.webclient.dto.PackageDetails;
import com.nasnav.shipping.services.bosta.webclient.dto.PackageSpec;
import com.nasnav.shipping.services.bosta.webclient.dto.Receiver;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class BostaLevisShippingService implements ShippingService{

	private Logger logger = LogManager.getLogger(getClass());
	
	
	private List<ServiceParameter> serviceParams;
	private Map<String,String> paramMap;
	public  static final String SERVICE_ID = "BOSTA_LEVIS" ;
	public static final String SERVICE_NAME = "Bosta";
	public static final BigDecimal FLAT_RATE = new BigDecimal("25.00");
	private static final ShippingPeriod DEFUALT_SHIPPING_PERIOD = 
			new ShippingPeriod(Period.ofDays(1), Period.ofDays(4));
	public static final String RETURN_EMAIL_MSG =
			"Thanks for patience! " +
			"Our delivery agents will contact you soon to receive the items from you." +
			"Please make sure to print the attached airway bills and provide them to the " +
			"delivery agent.";

	public static final String AUTH_TOKEN_PARAM = "AUTH_TOKEN";
	public static final String BUSINESS_ID_PARAM = "BUSINESS_ID";
	public static final String SERVER_URL = "SERVER_URL";
	public static final String WEBHOOK_URL = "WEBHOOK_URL";
	public static final String CAIRO_PRICE = "CAIRO_PRICE";
	public static final String ALEXANDRIA_PRICE = "ALEXANDRIA_PRICE";
	public static final String DELTA_CANAL_PRICE = "DELTA_CANAL_PRICE";
	public static final String UPPER_EGYPT_PRICE = "UPPER_EGYPT_PRICE";
	
	private static List<Parameter> SERVICE_PARAM_DEFINITION = 
			asList(new Parameter(AUTH_TOKEN_PARAM, STRING)
					, new Parameter(BUSINESS_ID_PARAM, STRING)
					, new Parameter(SERVER_URL, STRING)
					, new Parameter(WEBHOOK_URL, STRING)
					, new Parameter(CAIRO_PRICE, NUMBER)
					, new Parameter(ALEXANDRIA_PRICE, NUMBER)
					, new Parameter(DELTA_CANAL_PRICE, NUMBER)
					, new Parameter(UPPER_EGYPT_PRICE, NUMBER));
	
	private static final ShippingPeriodAndPrice CAIRO_SHIPPING = 
			new ShippingPeriodAndPrice(
					CAIRO_PRICE,
					new ShippingPeriod(Period.ofDays(1), Period.ofDays(2)));
	private static final ShippingPeriodAndPrice ALEXANDRIA_SHIPPING = 
			new ShippingPeriodAndPrice(
					ALEXANDRIA_PRICE,
					new ShippingPeriod(Period.ofDays(1), Period.ofDays(2)));
	private static final ShippingPeriodAndPrice DELTA_CANAL_SHIPPING = 
			new ShippingPeriodAndPrice(
					DELTA_CANAL_PRICE,
					new ShippingPeriod(Period.ofDays(2), Period.ofDays(3)));
	private static final ShippingPeriodAndPrice UPPER_EGYPT_SHIPPING = 
			new ShippingPeriodAndPrice(
					UPPER_EGYPT_PRICE,
					new ShippingPeriod(Period.ofDays(3), Period.ofDays(4)));
	
	
	private static final Map<Long,BostaCity> cityIdMapping = 
			ImmutableMap
				.<Long,BostaCity>builder()
			     .put(1L, new BostaCity("EG-01" ,CAIRO_SHIPPING))           
			     .put(2L, new BostaCity("EG-01" ,CAIRO_SHIPPING))            
			     .put(3L, new BostaCity("EG-02" ,ALEXANDRIA_SHIPPING))       
			     .put(13L, new BostaCity("EG-21" ,UPPER_EGYPT_SHIPPING))     
			     .put(14L, new BostaCity("EG-17" ,UPPER_EGYPT_SHIPPING))     
			     .put(15L, new BostaCity("EG-04" ,DELTA_CANAL_SHIPPING))     
			     .put(16L, new BostaCity("EG-16" ,UPPER_EGYPT_SHIPPING))     
			     .put(17L, new BostaCity("EG-05" ,DELTA_CANAL_SHIPPING))     
			     .put(18L, new BostaCity("EG-14" ,DELTA_CANAL_SHIPPING))     
			     .put(19L, new BostaCity("EG-15" ,UPPER_EGYPT_SHIPPING))     
			     .put(20L, new BostaCity("EG-07" ,DELTA_CANAL_SHIPPING))     
			     .put(21L, new BostaCity("EG-11" ,DELTA_CANAL_SHIPPING))     
			     .put(22L, new BostaCity("EG-08" ,DELTA_CANAL_SHIPPING))     
			     .put(23L, new BostaCity("EG-22" ,UPPER_EGYPT_SHIPPING))     
			     .put(25L, new BostaCity("EG-19" ,UPPER_EGYPT_SHIPPING))     
			     .put(26L, new BostaCity("EG-09" ,DELTA_CANAL_SHIPPING))     
			     .put(29L, new BostaCity("EG-13" ,DELTA_CANAL_SHIPPING))     
			     .put(30L, new BostaCity("EG-06" ,DELTA_CANAL_SHIPPING))     
			     .put(31L, new BostaCity("EG-20" ,UPPER_EGYPT_SHIPPING))     
			     .put(33L, new BostaCity("EG-10" ,DELTA_CANAL_SHIPPING))     
			     .put(34L, new BostaCity("EG-18" ,UPPER_EGYPT_SHIPPING))     
			     .put(36L, new BostaCity("EG-12" ,DELTA_CANAL_SHIPPING))     
			     .build();
	
	
	private static final Long PACKAGE = 10L;

	private static final List<Integer> enRouteStateMapping = asList(10, 15, 16, 35, 36);

	private static final List<Integer> pickedUpStateMapping = asList(20, 21, 30);

	private static final List<Integer> deliveredStateMapping = asList(22, 40, 25, 26, 23, 45);

	private static final List<Integer> failedStateMapping = asList(55, 80);

	
	
	@Autowired
	private ObjectMapper objectMapper;
	
	public BostaLevisShippingService() {
		paramMap = new HashMap<>();
	}
	
	
	
	@Override
	public ShippingServiceInfo getServiceInfo() {
		return new ShippingServiceInfo(SERVICE_ID, SERVICE_NAME, false, SERVICE_PARAM_DEFINITION, emptyList());
	}
	
	
	
	

	@Override
	public void setServiceParameters(List<ServiceParameter> params) {
		this.serviceParams = params;	
		this.paramMap = 
				this.serviceParams
				.stream()
				.filter(Objects::nonNull)
				.peek(this::validateParams)
				.collect(toMap(ServiceParameter::getParameter, ServiceParameter::getValue));
		
		if(paramMap.keySet().size() != SERVICE_PARAM_DEFINITION.size()) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID);
		}
	}
	
	
	
	

	@Override
	public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
		List<Optional<Shipment>> shipmentsOptionals = 
				IntStream
					.range(0, items.size())
					.mapToObj(i -> new IndexedData<>(i, items.get(i)))
					.map(this::createShipmentOffer)
					.collect(toList());
		
		if(anyShipmentCannotBeFulfilled(shipmentsOptionals)) {
			return Mono.empty();
		}
		
		List<Shipment> shipments = 
				shipmentsOptionals
					.stream()
					.map(Optional::get)
					.collect(toList());
		return Mono.just(new ShippingOffer(getServiceInfo(), shipments));
	}



	private boolean anyShipmentCannotBeFulfilled(List<Optional<Shipment>> shipmentsOptionals) {
		return shipmentsOptionals
				.stream()
				.anyMatch(shipment -> !shipment.isPresent());
	}

	
	
	
	
	@Override
	public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> shipments) {
		return Flux
				.fromIterable(shipments)
				.flatMap(this::requestSingleShipment);
	}



	private Mono<ShipmentTracker> requestSingleShipment(ShippingDetails shipment) {
		String serverUrl = 
				ofNullable(paramMap.get(SERVER_URL))
					.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0003, SERVER_URL, SERVICE_ID));
		
		String authToken = 
				ofNullable(paramMap.get(AUTH_TOKEN_PARAM))
					.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0003, AUTH_TOKEN_PARAM, SERVICE_ID));
		
		BostaWebClient client = new BostaWebClient(serverUrl);
		
		Delivery deliveryRequestDto = creatDeliveryRequestDto(shipment);
		return client
				.createDelivery(authToken, deliveryRequestDto)
				.flatMap(this::throwExceptionIfNotOk)
				.flatMap(res-> res.bodyToMono(CreateDeliveryResponse.class))
				.map(res -> new ShipmentTracker(res.getId(), res.getTrackingNumber(), null, shipment))
				.flatMap(shp -> getShipmentWithAirwaybill(authToken, client, shp));
	}



	private Mono<ShipmentTracker> getShipmentWithAirwaybill(String authToken, BostaWebClient client, ShipmentTracker shipment) {
		return client
				.createAirwayBill(authToken, shipment.getShipmentExternalId())
				.filter(res -> res.rawStatusCode() < 400)
				.flatMap(res -> res.bodyToMono(CreateAwbResponse.class))
				.map(CreateAwbResponse::getData)
				.defaultIfEmpty("")
				.map(bill -> new ShipmentTracker(shipment, bill));
	}
	
	
	
	
	
	private Delivery creatDeliveryRequestDto(ShippingDetails shipment) {
		String businessRef = 
				ofNullable(paramMap.get(BUSINESS_ID_PARAM))
					.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0003, BUSINESS_ID_PARAM, SERVICE_ID));
		
		Address pickupAddress =  createAddress(shipment.getSource());
		Address dropOffAddress = createAddress(shipment.getDestination());
		Receiver receiver = createReceiver(shipment.getReceiver());
		PackageSpec specs = createPackageSpecs(shipment);
		String notes = createShippingNotes(shipment);
		String webHook = paramMap.get(WEBHOOK_URL);

		Delivery request = new Delivery();
		request.setBusinessReference(businessRef);
		request.setDropOffAddress(dropOffAddress);
		request.setReceiver(receiver);
		request.setReturnAddress(pickupAddress);
		request.setType(PACKAGE);
		request.setPickupAddress(pickupAddress);
		request.setSpecs(specs);
		request.setNotes(notes);
		request.setWebhookUrl(webHook);
		request.setCod(shipment.getCodValue());
		return request;
	}
	
	
	
	
	
	private String createShippingNotes(ShippingDetails shipment) {
		return shipment
				.getItems()
				.stream()
				.map(this::createItemDetailsString)
				.collect(joining("\n\r"));
	}




	private String createItemDetailsString(ShipmentItems item) {
		return format("* name[%s]/ product Code[%s] / specs[%s] / qty[%d] "
				, ofNullable(item.getName()).orElse("")
				, ofNullable(item.getProductCode()).orElse("")
				, ofNullable(item.getSpecs()).orElse("")
				, ofNullable(item.getQuantity()).orElse(0));
	}




	private PackageSpec createPackageSpecs(ShippingDetails shipment) {
		String metaOrderId = 
				ofNullable(shipment.getMetaOrderId())
				.map(id -> id+"-")
				.orElse("");
		String description = format("Order Id: %s%d", metaOrderId, shipment.getSubOrderId());

		PackageDetails details = new PackageDetails();
		details.setDescription(description);
		details.setItemsCount(shipment.getItems().size());

		PackageSpec spec = new PackageSpec();
		spec.setPackageDetails(details);
		return spec;
	}



	private Receiver createReceiver(ShipmentReceiver user) {
		Receiver receiver = new Receiver();
		receiver.setEmail(user.getEmail());
		receiver.setFirstName(user.getFirstName());
		receiver.setLastName(user.getLastName());
		receiver.setPhone(getPhone(user));
		receiver.setCountry(user.getCountry());
		return receiver;
	};

	
	
	
	private String getPhone(ShipmentReceiver user) {
		return ofNullable(user.getPhone())
				.map(this::rectifyPhoneNumber)
				.orElse(null);
	}




	private String rectifyPhoneNumber(String phone) {
		return ofNullable(phone)
				.map(phn -> phn.replace("+", "0"))
				.map(phn -> leftPad(phn, 10, "0"))
				.orElse(phone);
	}




	private Address createAddress(ShippingAddress data) {
		Long apartmentNum = parseLongWithDefault(data.getFlatNumber(), null); 
		Long cityId = data.getCity();
		String cityExtId = 
				ofNullable(cityIdMapping.get(cityId))
				.map(BostaCity::getCityCode)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0005, SERVICE_ID, cityId));
		Address addr = new Address();
		addr.setApartment(apartmentNum);
		addr.setCity(cityExtId);
		addr.setFirstLine(data.getAddressLine1());
		addr.setSecondLine(data.getAddressLine2());
		addr.setBuildingNumber(data.getBuildingNumber());
		return addr;
	}

	
	

	private void validateParams(ServiceParameter param) {
		if(anyIsNull(param, param.getParameter(), param.getValue())) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0001, param);
		}
	}
	
	
	
	
	
	private Optional<Shipment> createShipmentOffer(IndexedData<ShippingDetails> details) {
		ShippingEta eta = calculateEta(details);
		List<Long> stocks = getStocks(details);
		return calculateFee(details)
				.filter(fee -> !stocks.isEmpty())
				.map(fee -> new Shipment(fee, eta, stocks, details.getData().getSubOrderId()));
	}

	
	


	private List<Long> getStocks(IndexedData<ShippingDetails> details) {
		return ofNullable(details.getData())
				.map(ShippingDetails::getItems)
				.orElse(emptyList())
				.stream()
				.map(ShipmentItems::getStockId)
				.collect(toList());
	}



	private ShippingEta calculateEta(IndexedData<ShippingDetails> details) {
		ShippingPeriod period = getShippingPeriod(details);
		return new  ShippingEta(now().plus(period.getFrom()), now().plus(period.getTo()));
	}



	private ShippingPeriod getShippingPeriod(IndexedData<ShippingDetails> details) {
		return getDestinationCityId(details)
				.map(cityIdMapping::get)
				.map(BostaCity::getShippingAndPriceInfo)
				.map(ShippingPeriodAndPrice::getShippingPeriod)
				.orElse(DEFUALT_SHIPPING_PERIOD);
	}



	private Optional<Long> getDestinationCityId(IndexedData<ShippingDetails> details) {
		return ofNullable(details)
				.map(IndexedData::getData)
				.map(ShippingDetails::getDestination)
				.map(ShippingAddress::getCity);
	}
	
	
	
	private Optional<Long> getPickupCityId(IndexedData<ShippingDetails> details) {
		return ofNullable(details)
				.map(IndexedData::getData)
				.map(ShippingDetails::getSource)
				.map(ShippingAddress::getCity);
	}



	private Optional<BigDecimal> calculateFee(IndexedData<ShippingDetails> details) {
		if( !isSupportedCity(details)) {
			return empty();
		}
		//curstomer pay for only first shipment, rest are free
		BigDecimal fee = getCityShippingFee(details);
		return Objects.equals(details.getIndex(), 0)? 
					Optional.of(fee) : Optional.of(ZERO);
	}



	private BigDecimal getCityShippingFee(IndexedData<ShippingDetails> details) {
		return getDestinationCityId(details)
				.map(cityIdMapping::get)
				.map(BostaCity::getShippingAndPriceInfo)
				.map(ShippingPeriodAndPrice::getPriceId)
				.map(paramMap::get)
				.map(BigDecimal::new)
				.map(fee -> fee.setScale(2, HALF_EVEN))
				.orElseThrow(() -> 
					new RuntimeBusinessException(INTERNAL_SERVER_ERROR
							, SHP$SRV$0012, SERVICE_ID, details.getData()));
	}



	private boolean isSupportedCity(IndexedData<ShippingDetails> details) {
		Boolean supportDestinationCity = getDestinationCityId(details).map(cityIdMapping::containsKey).orElse(false);
		Boolean supportPickupCity = getPickupCityId(details).map(cityIdMapping::containsKey).orElse(false); 
		return supportDestinationCity && supportPickupCity;
	}
	
	
	
	
	private Mono<ClientResponse> throwExceptionIfNotOk(ClientResponse response) {
		return just(response)
				.flatMap(this::checkResponse);
	}
	
	
	
	
	private Mono<ClientResponse> checkResponse(ClientResponse response){
		if(response.rawStatusCode() < 400) {
			return Mono.just(response);
		}else {
			return error( getFailedResponseRuntimeException(response));
		}
	}
	
	

	private RuntimeException getFailedResponseRuntimeException(ClientResponse response) {
		return new RuntimeBusinessException( INTERNAL_SERVER_ERROR, SHP$SRV$0004, SERVICE_ID, getResponseAsStr(response));
	}
	
	
	
	private String getResponseAsStr(ClientResponse response) {
		response
		.toEntity(String.class)
		.subscribe(res -> logger.info(format(" >>> shipping service [%s] failed, request returned response body [%s]" , SERVICE_ID, res.getBody())));
		return format("{status : %s}", response.statusCode());
	}



	@Override
	public void validateShipment(List<ShippingDetails> items) {
		createShippingOffer(items)
			.map(offer -> new ShipmentValidation(true))
			.blockOptional(Duration.ofSeconds(30))
			.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0010));
	}

	@Override
	public ShipmentStatusData createShipmentStatusData(String serviceId, Long orgId, String params){
		BostaCallbackDTO body;
		try {
			body = objectMapper.readValue(params, BostaCallbackDTO.class);
		} catch (IOException e) {
			logger.error(e, e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, G$JSON$0001);
		}
		Integer shippingStatus = getShippingStatus(body.getState());

		return new ShipmentStatusData(serviceId, orgId, body.getId(), shippingStatus, body.getExceptionReason());
	}


	private Integer getShippingStatus(Integer state) {
		if (enRouteStateMapping.contains(state)) {
			return EN_ROUTE.getValue();
		} else if (pickedUpStateMapping.contains(state)) {
			return PICKED_UP.getValue();
		} else if (deliveredStateMapping.contains(state)) {
			return DELIVERED.getValue();
		} else if (failedStateMapping.contains(state)) {
			return FAILED.getValue();
		} else {
			return state;
		}
	}



	@Override
	public Flux<ReturnShipmentTracker> requestReturnShipment(List<ShippingDetails> items) {
		return requestShipment(items)
				.map(shpTracker -> new ReturnShipmentTracker(shpTracker, RETURN_EMAIL_MSG));
	}


}





@Data
@AllArgsConstructor
class ShippingPeriodAndPrice{
	private String priceId;
	private ShippingPeriod shippingPeriod;
}



@Data
@AllArgsConstructor
class BostaCity{
	private String cityCode;
	private ShippingPeriodAndPrice shippingAndPriceInfo;
}
