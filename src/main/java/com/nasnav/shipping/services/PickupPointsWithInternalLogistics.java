package com.nasnav.shipping.services;

import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.service.model.common.ParameterType.LONG_ARRAY;
import static com.nasnav.service.model.common.ParameterType.NUMBER;
import static com.nasnav.shipping.model.ShippingServiceType.PICKUP;
import static java.lang.Long.MIN_VALUE;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.time.LocalDate.now;
import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.context.i18n.LocaleContextHolder.getLocale;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import javax.annotation.PostConstruct;

import com.nasnav.service.DomainService;
import com.nasnav.shipping.model.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;
import org.thymeleaf.templatemode.TemplateMode;
import org.thymeleaf.templateresolver.ClassLoaderTemplateResolver;
import org.thymeleaf.templateresolver.ITemplateResolver;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.shipping.ShippingService;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public class PickupPointsWithInternalLogistics implements ShippingService{

	private final Logger logger = LogManager.getLogger();
	public static final String SERVICE_ID = "PICKUP_POINTS"; 
	public static final String SERVICE_NAME = "Pickup point";
	public static final String ICON = "/icons/pickup_from_shop_logo.svg";
	public static final String WAREHOUSE_ID = "WAREHOUSE_ID";
	public static final String SHOP_ID = "SHOP_ID";
	public static final String PICKUP_POINTS_ID_LIST = "PICKUP_POINTS_ID_LIST";
	public static final String RETURN_EMAIL_MSG =
			"Thanks for you patience! To complete the return process, please return " +
					" the items back to shop and provide the sellers with this email!";
	public static final String ETA_DAYS_MIN = "ETA_DAYS_MIN";
	public static final String ETA_DAYS_MAX = "ETA_DAYS_MAX";

	private static final Integer ETA_DAYS_MIN_DEFAULT = 1;
	private static final Integer ETA_DAYS_MAX_DEFAULT = 7;

	private static List<Parameter> SERVICE_PARAM_DEFINITION = 
			asList(new Parameter(WAREHOUSE_ID, NUMBER)
					, new Parameter(PICKUP_POINTS_ID_LIST, LONG_ARRAY)
					, new Parameter(ETA_DAYS_MIN, NUMBER, false)
					, new Parameter(ETA_DAYS_MAX, NUMBER, false));
	
	private static List<Parameter> ADDITIONAL_PARAM_DEFINITION = 
			asList(new Parameter(SHOP_ID, NUMBER));
	
	private static String SHIPPING_REPORT_TEMPLATE = "shipping_report.html";
	
	private Long warehouseId;
	private Set<Long> allowedShops;
	private Integer etaDaysMin;
	private Integer etaDaysMax;
	
	
	@Autowired
	ShopsRepository shopRepo;
	
	@Autowired
	SecurityService securityService;
	@Autowired
	DomainService domainService;
	
	private  SpringTemplateEngine templateEngine;
    
    
    @PostConstruct
    public void init() {
        templateEngine = new SpringTemplateEngine();
        templateEngine.addTemplateResolver(htmlTemplateResolver());
    }
	
	public PickupPointsWithInternalLogistics() {
    	allowedShops = emptySet();
		etaDaysMin = ETA_DAYS_MIN_DEFAULT;
		etaDaysMax = ETA_DAYS_MAX_DEFAULT;
	}
	
	
	
	
	@Override
	public ShippingServiceInfo getServiceInfo() {
		return new ShippingServiceInfo(
					SERVICE_ID
					, SERVICE_NAME
					, true
					, SERVICE_PARAM_DEFINITION
					, ADDITIONAL_PARAM_DEFINITION
					, PICKUP
					, ICON);
	}
	
	
	
	private ITemplateResolver htmlTemplateResolver() {
        final ClassLoaderTemplateResolver templateResolver = new ClassLoaderTemplateResolver();
//        templateResolver.setApplicationContext(applicationContext);
        templateResolver.setOrder(Integer.valueOf(1));
//        templateResolver.setResolvablePatterns(singleton("html/*"));
        templateResolver.setPrefix("/templates/shipping/pickup_points/");
        templateResolver.setSuffix(".html");
        templateResolver.setTemplateMode(TemplateMode.HTML);
        templateResolver.setCharacterEncoding("UTF-8");
        templateResolver.setCacheable(true);
        return templateResolver;
    }
	
	
	private String createHtmlFromThymeleafTemplate(String template, Map<String,Object> variables) {
		Context ctx = new Context(getLocale());
		ctx.setVariables(variables);
		return this.templateEngine.process(template, ctx);
	}

	
	
	
	@Override
	public void setServiceParameters(List<ServiceParameter> params) {
		Map<String, String> serviceParams =
				params
				.stream()
				.collect(toMap(ServiceParameter::getParameter, ServiceParameter::getValue, (v1, v2) -> v1));
		setAllowedShops(serviceParams);
		setWarehouseId(serviceParams);
		setEtaDaysMin(serviceParams);
		setEtaDaysMax(serviceParams);
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



	private void setWarehouseId(Map<String, String> serviceParams) {
		warehouseId =
			ofNullable(serviceParams.get(WAREHOUSE_ID))
				.flatMap(EntityUtils::parseLongSafely)
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SRV$0002, SERVICE_ID));
	}



	private void setAllowedShops(Map<String, String> serviceParams) {
		String allowedShopsJson = ofNullable(serviceParams.get(PICKUP_POINTS_ID_LIST)).orElse("[]");
		allowedShops =
				streamJsonArrayElements(allowedShopsJson)
				.map(Object::toString)
				.map(EntityUtils::parseLongSafely)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.collect(toSet());
	}



	Stream<Object> streamJsonArrayElements(String jsonString){
		return ofNullable(jsonString)
				.map(JSONArray::new)
				.map(JSONArray::spliterator)
				.map(iterator -> StreamSupport.stream(iterator, false))
				.orElse(Stream.empty());
	}



	@Override
	public Mono<ShippingOffer> createShippingOffer(List<ShippingDetails> items) {
		List<String> pickupShops = getPickupShops();
		
		ShippingServiceInfo serviceInfo = createServiceInfoWithShopsOptions(pickupShops);
		
		List<Shipment> shipments =
				items
				.stream()
				.map(this::createShipment)
				.collect(toList());
		
		ShippingOffer offer = new ShippingOffer(serviceInfo, shipments);
		return pickupShops.isEmpty()? 
				Mono.empty() : Mono.just(offer) ;
	}
	
	
	
	
	
	private List<String> getPickupShops(){
		return allowedShops
				.stream()
				.map(id -> id.toString())
				.collect(toList());
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
	
	
	
	
	private Shipment createShipment(ShippingDetails shippingDetails) {
		BigDecimal fee = ZERO;
		ShippingEta eta = new ShippingEta(now().plusDays(etaDaysMin), now().plusDays(etaDaysMax));
		List<Long> stocks = 
				shippingDetails
				.getItems()
				.stream()
				.map(ShipmentItems::getStockId)
				.collect(toList());
		Long orderId = shippingDetails.getSubOrderId();
		return new Shipment(fee, eta, stocks ,orderId);
	}

	
	
	@Override
	public Flux<ShipmentTracker> requestShipment(List<ShippingDetails> items) {
		//need to return csv containing items - selected pickup point - addresses 
		//- order and meta order as the airwaybill
		ShippingDetails details =
				items
				.stream()
				.findFirst()
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0010));
		ShipmentTracker tracker = new ShipmentTracker();
		String reportBase64String = createShipmentReport(items);
		tracker.setAirwayBillFile(reportBase64String);
		tracker.setShippingDetails(details);
		return Flux.just(tracker);
	}




	private String createShipmentReport(List<ShippingDetails> shipmentDetails) {
		List<ShipmentData> shipments = createShipmentReportData(shipmentDetails);
		String report = processReportTemplate(shipments);
		return convertToBase64String(report);
	}




	private String convertToBase64String(String report) {
		return Base64.getEncoder().encodeToString(report.getBytes());
	}




	private String processReportTemplate(List<ShipmentData> shipments) {
		Map<String, Object> templateVariables = 
				MapBuilder
				.<String,Object>map()
				.put("shipments", shipments)
				.getMap();
		return createHtmlFromThymeleafTemplate(SHIPPING_REPORT_TEMPLATE, templateVariables);
	}




	private List<ShipmentData> createShipmentReportData(List<ShippingDetails> shipmentDetails) {
		return shipmentDetails
				.stream()
				.map(this::createShipmentData)
				.collect(toList());
	}

	
	
	
	private ShipmentData createShipmentData(ShippingDetails shippingDetails) {
		String fullName = shippingDetails.getReceiver().getFirstName()
							+" "+ shippingDetails.getReceiver().getLastName();
		String orderFullId = "" + shippingDetails.getMetaOrderId() + "-" +shippingDetails.getSubOrderId();
		ShopData shopData = createShopData(shippingDetails);
		
		ShipmentData data = new ShipmentData();
		data.setCustomerName(fullName);
		data.setCustomerPhone(shippingDetails.getReceiver().getPhone());
		data.setOrderFullId(orderFullId);
		data.setShop(shopData);
		data.setItems(shippingDetails.getItems());
		return data;
	}

	

	private ShopData createShopData(ShippingDetails shippingDetails) {
		ShopsEntity shop = 
				ofNullable(shippingDetails)
				.map(ShippingDetails::getAdditionalData)
				.map(additionalData -> additionalData.get(SHOP_ID))
				.flatMap(EntityUtils::parseLongSafely)
				.flatMap(shopRepo::findShopFullData)
				.orElseThrow(() -> {
					String message = "Selected shop is not a valid pickup point!";
					return new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0011, message);
				});
		String addressString = createAddressString(shop);
		return new ShopData(shop.getId(), shop.getName(), addressString);
	}




	private String createAddressString(ShopsEntity shop) {
		AddressRepObj addressData = ((ShopRepresentationObject)shop.getRepresentation()).getAddress();
		return format("%s,%s,%s,%s%s%s%s"
				, addressData.getCountry()
				, addressData.getCity()
				, addressData.getArea()
				, addressData.getAddressLine1()
				, createAddressElement(addressData.getAddressLine2())
				, createAddressElement(addressData.getBuildingNumber())
				, createAddressElement(addressData.getFlatNumber()));
	}




	private String createAddressElement(String element) {
		return ofNullable(element)
				.map(e -> ","+e)
				.orElse("");
	}




	@Override
	public void validateShipment(List<ShippingDetails> items) {
		boolean isCartFromSingleShop = items.size() == 1;
		boolean isCartStocksFromWarehouse = isCartStocksFromWarehouseOnly(items);
		boolean isShopAllowedForPickup = isShopAllowedForPickup(items);
		
		String message = "";
		if(!isCartFromSingleShop) {
			message = "Cart has items from multiple shops, while pickup points service is allowed "
					+ "for a single shop!";
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0011, message);
		}else if(!isShopAllowedForPickup) {
			message = "Selected shop is not a valid pickup point!";
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0011, message);
		}else if(!isCartStocksFromWarehouse) {
			message = "Cart items are not from the warehouse!";
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0011, message);
		}
	}
	
	
	
	
	private boolean isCartStocksFromWarehouseOnly(List<ShippingDetails> items) {
		return items
				.stream()
				.allMatch(shippingDetails -> Objects.equals(shippingDetails.getShopId(), warehouseId));
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
    	return items
				.stream()
				.map(this::createReturnShipmentTracker)
				.collect(collectingAndThen(toList(), Flux::fromIterable));
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



	private ReturnShipmentTracker createReturnShipmentTracker(ShippingDetails item){
		return new ReturnShipmentTracker(new ShipmentTracker(item), RETURN_EMAIL_MSG);
	}
}



@Data
class ShipmentData{
	private ShopData shop;
	private String customerName;
	private String customerPhone;
	private String orderFullId;
	private List<ShipmentItems> items;
}


@Data
@AllArgsConstructor
class ShopData{
	private Long id;
	private String name;
	private String address;
}



@Data
@AllArgsConstructor
class Item{
	private String name;
	private String productCode;
	private String barcode;
	private String specs;
}