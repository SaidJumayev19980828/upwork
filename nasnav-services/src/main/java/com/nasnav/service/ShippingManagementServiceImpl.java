package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.json.jackson.RawObject;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.commons.utils.MapBuilder;
import com.nasnav.dao.*;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.*;
import com.nasnav.dto.response.OrderConfirmResponseDTO;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.persistence.dto.query.result.CartCheckoutData;
import com.nasnav.persistence.dto.query.result.CartItemShippingData;
import com.nasnav.service.model.common.Parameter;
import com.nasnav.service.model.common.ParameterType;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Setter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.ZoneId;
import java.util.*;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.enumerations.OrderStatus.DISPATCHED;
import static com.nasnav.enumerations.ShippingStatus.*;
import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.payments.cod.CodCommons.COD_OPERATOR;
import static com.nasnav.service.model.common.ParameterType.STRING;
import static com.nasnav.shipping.model.CommonServiceParameters.CART_OPTIMIZER;
import static com.nasnav.shipping.model.Constants.DEFAULT_AWB_FILE_MIME;
import static com.nasnav.shipping.model.Constants.DEFAULT_AWB_FILE_NAME;
import static com.nasnav.shipping.model.ShippingServiceType.PICKUP;
import static java.lang.String.format;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.*;
import static org.springframework.http.HttpStatus.*;

@Service
public class ShippingManagementServiceImpl implements ShippingManagementService {
	
	private static final String SHIPPING_SERVICE_CALLBACK_TEMPLATE = "callbacks/shipping/service/%s/%d";

	private Logger logger = LogManager.getLogger(getClass());
	
	private static final Map<Integer,OrderStatus> shippingStatusToOrderStatusMapping = 
			MapBuilder
			.<Integer, OrderStatus>map()
			.put(PICKED_UP.getValue(), DISPATCHED)
			.put(EN_ROUTE.getValue(), DISPATCHED)
			.put(ShippingStatus.DELIVERED.getValue(), OrderStatus.DELIVERED)
			.getMap();

	@Autowired
	private OrganizationShippingServiceRepository orgShippingServiceRepo;
	@Autowired
	private CartItemRepository cartRepo;
	@Autowired
	private AddressRepository addressRepo;
	@Autowired
	private StockRepository stockRepo;
	@Autowired
	private UserRepository userRepo;
	@Autowired
	private ShipmentRepository shipmentRepo;
	@Autowired
	private ShopsRepository shopRepo;
	@Autowired
	private PaymentsRepository paymentRepo;
	@Autowired
	private ReturnShipmentRepository returnShipmentRepo;
	@Autowired
	private ReturnRequestItemRepository returnedItemRepo;
	@Autowired
	private OrdersRepository orderRepo;

	@Autowired
	@Setter
	private SecurityService securityService;
	@Autowired
	private DomainService domainService;
	@Autowired
	private OrderService orderService;
	@Autowired
	private PromotionsService promotionsService;
	@Autowired
	private ProductService productService;

	@Autowired
	private ObjectMapper jsonMapper;

	@Autowired
    private ShippingServiceFactory shippingServiceFactory;

	@Override
	public List<ShippingOfferDTO> getShippingOffers(Long customerAddrId) {
		List<ShippingDetails> shippingDetails = createShippingDetailsFromCurrentCart(customerAddrId);
		
		return getOffersFromOrganizationShippingServices(shippingDetails);
	}



	@Override
	public void validateCartForShipping(List<CartCheckoutData> cartItemData, CartCheckoutDTO dto) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ShippingService shippingService = getShippingService(dto.getServiceId(), orgId);

		List<ShippingDetails> shippingDetails = createShippingDetailsFromCartCheckoutData(cartItemData, dto.getAddressId());
		for(ShippingDetails shippingDetail : shippingDetails) {
			shippingDetail.setAdditionalData(dto.getAdditionalData());
		}

		shippingService.validateShipment(shippingDetails);
	}


	
	
	
	private List<ShippingDetails> createShippingDetailsFromCartCheckoutData(List<CartCheckoutData> cartItemData, Long addressId) {
		List<CartItemShippingData> cartShippingData = 
				cartItemData
				.stream()
				.map(this::createCartShippingData)
				.collect(toList());
		return createShippingDetailsFromCartItemShippingData(cartShippingData, addressId);
	}

	
	
	
	private CartItemShippingData createCartShippingData(CartCheckoutData itemCheckoutData) {
		Long stockId = itemCheckoutData.getStockId();
		Long shopId = itemCheckoutData.getShopId();
		Long shopAddressId = itemCheckoutData.getShopAddress().getId();
		BigDecimal price = itemCheckoutData.getPrice();
		BigDecimal discount = itemCheckoutData.getDiscount();
		Integer quantity = itemCheckoutData.getQuantity();
		BigDecimal weight = itemCheckoutData.getWeight();
		return new CartItemShippingData(stockId, shopId, shopAddressId, price, discount, quantity, weight);
	}
	
	


	private ShippingService getShippingService(String serviceId, Long orgId) {
		Optional<ShippingService> shippingService =
					orgShippingServiceRepo
					.getByOrganization_IdAndServiceId(orgId, serviceId)
					.map(this::getShippingService)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SHIP$0001, serviceId));

		if (!shippingService.isPresent()) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SVC$0001);
		}
		return shippingService.get();
	}


	
	
	
	@Override
	public List<ShippingOfferDTO> getOffersFromOrganizationShippingServices(List<ShippingDetails> shippingDetails) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		return Flux
				.fromIterable(orgShippingServiceRepo.getByOrganization_Id(orgId))
				.map(this::getShippingService)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.flatMap(service -> service.createShippingOffer(shippingDetails))
				.map(this::createShippingOfferDTO)
				.collectList()
				.blockOptional()
				.orElse(emptyList());
	}
	
	private ShipmentDTO setShippingPromoDiscount(ShipmentDTO dto) {
		BigDecimal totalCartValue = cartRepo.findTotalCartValueByUser_Id(securityService.getCurrentUser().getId());
		BigDecimal discount = promotionsService.calculateShippingPromoDiscount(dto.getShippingFee(), totalCartValue);
		dto.setShippingFee(dto.getShippingFee().subtract(discount));
		return dto;
	}
	
	
	private ShippingOfferDTO createShippingOfferDTO(ShippingOffer data) {
		String icon = domainService.getBackendUrl() + data.getService().getIcon();

		ShippingOfferDTO offerDto = new ShippingOfferDTO();
		List<ShippingAdditionalDataDTO> additionalParams = getAdditionalParametersDtoList(data);
		List<ShipmentDTO> shipments = getShipmentDtoList(data);
		BigDecimal total = calculateTotal(shipments);
		offerDto.setAdditionalData(additionalParams);
		offerDto.setServiceId(data.getService().getId());
		offerDto.setServiceName(data.getService().getName());
		offerDto.setShipments(shipments);
		offerDto.setTotal(total);
		offerDto.setType(data.getService().getType().name());
		offerDto.setIcon(icon);
		offerDto.setAvailable(data.isAvailable());
		offerDto.setMessage(data.getMessage());
		return offerDto;
	}




	private BigDecimal calculateTotal(List<ShipmentDTO> shipments) {
		return ofNullable(shipments)
				.orElse(emptyList())
				.stream()
				.map(ShipmentDTO::getShippingFee)
				.reduce(ZERO, BigDecimal::add);
	}




	private List<ShipmentDTO> getShipmentDtoList(ShippingOffer data) {
		return data
				.getShipments()
				.stream()
				.map(this::createShipmentDTO)
				.map(this::setShippingPromoDiscount)
				.collect(toList());
	}


	
	
	private ShipmentDTO createShipmentDTO(Shipment shipment) {
		ShippingEtaDTO etaDto = 
				ofNullable(shipment.getEta())
				.map(eta -> new ShippingEtaDTO(
						eta.getFrom().atZone(ZoneId.of("UTC")),
						eta.getTo().atZone(ZoneId.of("UTC"))))
				.orElse(new ShippingEtaDTO() );
		
		List<Long> stocks = shipment.getStocks();
		StocksEntity stock = 
				stocks
				.stream()
				.findFirst()
				.flatMap(stockRepo::findWithAdditionalData)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$OFFR$0001 ));
		
		ShipmentDTO dto = new ShipmentDTO();
		dto.setEta(etaDto);
		dto.setShippingFee(shipment.getShippingFee());
		dto.setShopId(stock.getShopsEntity().getId());
		dto.setShopName(stock.getShopsEntity().getName());
		dto.setStocks(stocks);
		dto.setSubOrderId(shipment.getSubOrderId());
		return dto;
	}

	
	
	

	private List<ShippingAdditionalDataDTO> getAdditionalParametersDtoList(ShippingOffer offer) {
		return offer
				.getService()
				.getAdditionalDataParams()
				.stream()
				.map(this::createShippingAdditionalDataDTO)
				.collect(toList());
	}
	
	
	
	
	private ShippingAdditionalDataDTO createShippingAdditionalDataDTO(Parameter param) {
		String type = ofNullable(param.getType())
						.map(ParameterType::getValue)
						.orElse(STRING.getValue());
		return new ShippingAdditionalDataDTO(param.getName(), type, param.getOptions());
	}
	
	
	
	
	@Override
	public Optional<ShippingService> getShippingService(OrganizationShippingServiceEntity orgShippingService){
		String id = orgShippingService.getServiceId();
		List<ServiceParameter> serviceParameters = parseServiceParameters(orgShippingService);
		
		return shippingServiceFactory.getShippingService(id, serviceParameters);
	}

	
	
	

	public List<ServiceParameter> parseServiceParameters(OrganizationShippingServiceEntity orgShippingService) {
		String serviceParamsString = ofNullable(orgShippingService.getServiceParameters()).orElse("{}");
		
		TypeReference<HashMap<String, RawObject>> typeRef = new TypeReference<HashMap<String, RawObject>>() {};
		Map<String, RawObject> paramMap = new HashMap<>();
		try {
			paramMap = jsonMapper.readValue(serviceParamsString, typeRef) ;
		} catch (Exception e) {
			logger.error(e,e);
		}
		return paramMap
				.entrySet()
				.stream()
				.map(e -> new ServiceParameter(e.getKey(), e.getValue().getValue()))
				.collect(toList());
	}

	
	
	
	private List<ShippingDetails> createShippingDetailsFromCurrentCart(Long customerAddrId) {
		Long userId = securityService.getCurrentUser().getId();
		List<CartItemShippingData> cartData = cartRepo.findCartItemsShippingDataByUser_Id(userId);
		return createShippingDetailsFromCartItemShippingData(cartData, customerAddrId);
	}



	
	
	private List<ShippingDetails> createShippingDetailsFromCartItemShippingData(List<CartItemShippingData> cartData, Long customerAddrId) {
		Map<Long, AddressesEntity> addresses = getAddresses(customerAddrId, cartData);
		
		validateCartItemShops(cartData);
		
		return cartData
				.stream()
				.collect(groupingBy(this::getShopAndItsAddress))
				.entrySet()
				.stream()
				.map(itemsPerAddr -> createShippingDetails(itemsPerAddr, addresses, customerAddrId))
				.collect(toList());
	}




	private void validateCartItemShops(List<CartItemShippingData> cartData) {
		cartData
		.stream()
		.map(this::getShopAndItsAddress)
		.filter(ShopAndItsAddress::anyIsNull)
		.forEach( shpAndAddr -> {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, S$0004 , shpAndAddr.getShopId(), shpAndAddr.getAddressId());});
	}
	
	
	
	
	private ShopAndItsAddress getShopAndItsAddress(CartItemShippingData item) {
		return new ShopAndItsAddress(item.getShopId(), item.getShopAddressId());
	}


	
	
	private ShippingDetails createShippingDetails(Map.Entry<ShopAndItsAddress, List<CartItemShippingData>> entry
			, Map<Long, AddressesEntity> addresses, Long customerAddrId) {
		Long shopAddressId = entry.getKey().getAddressId();
		List<ShipmentItems> items = 
				entry
				.getValue()
				.stream()
				.map(this::createShipmentItem)
				.collect(toList());
		
		AddressesEntity shopAddress = 
				ofNullable(addresses.get(shopAddressId))
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, ADDR$ADDR$0002, shopAddressId));
		
		AddressesEntity customerAddress =
				ofNullable(addresses.get(customerAddrId))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ADDR$ADDR$0002, customerAddrId));
		
		ShippingAddress pickupAddr = createShippingAddress(shopAddress);
		ShippingAddress customerAddr = createShippingAddress( customerAddress); 
		
		ShippingDetails shippingDetails = new ShippingDetails();
		shippingDetails.setDestination(customerAddr);
		shippingDetails.setSource(pickupAddr);
		shippingDetails.setItems(items);
		shippingDetails.setShopId(entry.getKey().getShopId());
		return shippingDetails;
	}
	
	
	
	
	
	private ShippingAddress createShippingAddress(AddressesEntity entity) {
		Optional<SubAreasEntity> subArea =  ofNullable(entity).map(AddressesEntity::getSubAreasEntity);
		Optional<AreasEntity> area = ofNullable(entity).map(AddressesEntity::getAreasEntity);
		Optional<CitiesEntity> city = area.map(AreasEntity::getCitiesEntity);
		Optional<CountriesEntity> country = city.map(CitiesEntity::getCountriesEntity);

		Long subAreaId = subArea.map(SubAreasEntity::getId).orElse(-1L);
		Long areaId = area.map(AreasEntity::getId).orElse(-1L);
		Long cityId = city.map(CitiesEntity::getId).orElse(-1L);
		Long countryId = country.map(CountriesEntity::getId).orElse(-1L);
		
		BaseUserEntity user = securityService.getCurrentUser();
		
		ShippingAddress addr = new ShippingAddress();
		addr.setAddressLine1(entity.getAddressLine1());
		addr.setAddressLine2(entity.getAddressLine2());
		addr.setArea(areaId);
		addr.setSubArea(subAreaId);
		addr.setBuildingNumber(entity.getBuildingNumber());
		addr.setCity(cityId);
		addr.setCountry(countryId);
		addr.setFlatNumber(entity.getFlatNumber());
		addr.setId(entity.getId());
		addr.setLatitude(entity.getLatitude());
		addr.setLongitude(entity.getLongitude());
		addr.setName(user.getName());
		addr.setPostalCode(entity.getPostalCode());
		return addr;
	}


	private ShipmentItems createShipmentItem(CartItemShippingData data) {
		BigDecimal discount = ofNullable(data.getDiscount()).orElse(ZERO);
		ShipmentItems shippingItem = new ShipmentItems(data.getStockId());
		shippingItem.setPrice(data.getPrice().subtract(discount));
		shippingItem.setQuantity(data.getQuantity());
		shippingItem.setWeight(data.getWeight());
		return shippingItem;
	}
	
	
	
	
	
	private Map<Long, AddressesEntity> getAddresses(Long customerAddrId, List<CartItemShippingData> cartData) {
		List<Long> addrIds = 
				cartData
				.stream()
				.map(CartItemShippingData::getShopAddressId)
				.filter(Objects::nonNull)
				.collect(toList());
		
		addrIds.add(customerAddrId);
		 return	addressRepo
					.findByIdIn(addrIds)
					.stream()
					.collect(toMap(AddressesEntity::getId, addr -> addr));
	}




	@Override
	public void registerToShippingService(ShippingServiceRegistration registration) {
		String serviceId = registration.getServiceId(); 
		ShippingServiceInfo info = 
				shippingServiceFactory
					.getServiceInfo(serviceId)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE , SHP$SRV$0006, serviceId));
		
		String serviceParamsString = 
				ofNullable(registration.getServiceParameters())
				.map(this::serialize)
				.map(str -> str.isEmpty()? "{}": str)
				.orElse("{}");
		
		validateServiceParameters(serviceId, info, serviceParamsString);
		
		persistServiceParameters(serviceId, serviceParamsString);
	}
	
	
	
	
	
	private String serialize(Map<String,Object> map) {
		try {
			return jsonMapper.writeValueAsString(map);
		} catch (JsonProcessingException e) {
			logger.error(e,e);
			throw new RuntimeException(e); 
		}
	}




	private void persistServiceParameters(String serviceId, String serviceParamsString) {
		OrganizationEntity organization = securityService.getCurrentUserOrganization();
		OrganizationShippingServiceEntity orgService = 
				orgShippingServiceRepo
				.getByOrganization_IdAndServiceId(organization.getId(), serviceId)
				.orElse(new OrganizationShippingServiceEntity());
		
		orgService.setServiceId(serviceId);
		orgService.setServiceParameters(serviceParamsString);
		orgService.setOrganization(organization);
		
		orgShippingServiceRepo.save(orgService);
	}




	private void validateServiceParameters(String serviceId, ShippingServiceInfo info, String serviceParamsString) {
		try {
			JSONObject paramsJson = new JSONObject(serviceParamsString);
			info
			.getServiceParams()
			.stream()
			.forEach(param -> validateServiceParameter(param, paramsJson, serviceId));
		}catch(JSONException t) {
			logger.error(t,t);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0007, serviceParamsString);
		}
	}
	
	
	
	
	
	private void validateServiceParameter(Parameter param, JSONObject paramsJson, String serviceId) {
		String name = param.getName();
		try {
			Optional<?> paramVal =
					ofNullable(paramsJson)
					.filter(json -> json.has(name))
					.map(json -> json.get(name));
			if(param.isRequired() && !paramVal.isPresent()) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0003, name, serviceId);
			}
			if(!isValidType(param.getType(), paramVal)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0008, paramVal.get().toString(), name, serviceId);
			}
		}catch(JSONException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0003, name, serviceId);
		}
	}



	private Boolean isValidType(ParameterType type, Optional<?> paramVal) {
		return paramVal
				.map(val -> type.getJavaType().isInstance(val))
				.orElse(true);
	}


	@Override
	@Transactional
	public Mono<ShipmentTracker> requestShipment(OrdersEntity subOrder) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		
		ShippingDetails shippingDetails = createShippingDetailsFromOrder(subOrder);
		ShipmentEntity shipment = subOrder.getShipment();
		return ofNullable(shipment)
				.map(ShipmentEntity::getShippingServiceId)
				.flatMap(serviceId -> orgShippingServiceRepo.getByOrganization_IdAndServiceId(orgId, serviceId))
				.flatMap(this::getShippingService)
				.map(service -> service.requestShipment(asList(shippingDetails)))
				.map(flux -> flux.map(this::setDefaultTrackerDataIfNeeded))
				.map(Flux::singleOrEmpty)
				.orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0001, subOrder.getId()));
	}


	private ShipmentTracker setDefaultTrackerDataIfNeeded(ShipmentTracker tracker) {
		var mod = new ShipmentTracker(tracker, tracker.getAirwayBillFile());
		if(isNull(tracker.getAirwayBillFileName()) ){
			mod.setAirwayBillFileName(DEFAULT_AWB_FILE_NAME);
			mod.setAirwayBillFileMime(DEFAULT_AWB_FILE_MIME);
		}
		return mod;
	}


	@Override
	public ShippingDetails createShippingDetailsFromOrder(OrdersEntity subOrder) {
		String params = 
				ofNullable(subOrder.getShipment())
				.map(ShipmentEntity::getParameters)
				.orElse("{}");
		Map<String,String> additionalData = parseJsonAsMap(params);
		return createShippingDetailsFromOrder(subOrder, additionalData);
	}
	
	
	
	@Override
	public ShippingDetails createShippingDetailsFromOrder(OrdersEntity subOrder, Map<String,String> additionalParameters) {
		ShippingDetails shippingData = new ShippingDetails();
		ShippingAddress customerAddr = createShippingAddress(subOrder.getAddressEntity());
		ShippingAddress shopAddr = createShippingAddress(subOrder.getShopsEntity().getAddressesEntity());
		ShipmentReceiver receiver = createShipmentReceiver(subOrder);
		List<ShipmentItems> items = createShipmentItemsFromOrder(subOrder);
		String callBackUrl = createCallBackUrl(subOrder);
		Long metaOrderId = 
				ofNullable(subOrder)
				.map(OrdersEntity::getMetaOrder)
				.map(MetaOrderEntity::getId)
				.orElse(null);
		
		shippingData.setAdditionalData(additionalParameters);
		shippingData.setDestination(customerAddr);
		shippingData.setReceiver(receiver);
		shippingData.setSource(shopAddr);
		shippingData.setItems(items);
		shippingData.setSubOrderId(subOrder.getId());
		shippingData.setMetaOrderId(metaOrderId);
		shippingData.setCallBackUrl(callBackUrl);
		shippingData.setShopId(subOrder.getShopsEntity().getId());
		if(isPaidByCashOnDelivery(subOrder)) {
			shippingData.setCodValue(subOrder.getTotal());
		}
		return shippingData;
	}



    private boolean isPaidByCashOnDelivery(OrdersEntity subOrder) {
		return ofNullable(subOrder)
				.map(OrdersEntity::getMetaOrder)
				.map(MetaOrderEntity::getId)
				.flatMap(paymentRepo::findByMetaOrderId)
				.map(PaymentEntity::getOperator)
				.filter(operator -> Objects.equals(operator, COD_OPERATOR))
				.isPresent();
	}



	private String createCallBackUrl(OrdersEntity subOrder) {
    	String serviceId = 
    			ofNullable(subOrder)
    			.map(OrdersEntity::getShipment)
    			.map(ShipmentEntity::getShippingServiceId)
    			.orElse("INVALID");
    	Long orgId = securityService.getCurrentUserOrganizationId();
    	String callBackUrl = format(SHIPPING_SERVICE_CALLBACK_TEMPLATE, serviceId, orgId);
    	String domain = domainService.getBackendUrl();
    	return format("%s/%s", domain, callBackUrl);
	}



	@Override
	@Transactional(rollbackOn = Throwable.class)
    public void updateShipmentStatus(String serviceId, Long orgId, String params) throws IOException {
		ShippingService shippingService = getShippingService(serviceId, orgId);
		ShipmentStatusData shippingStatusData = shippingService.createShipmentStatusData(serviceId, orgId, params);

		if (shippingStatusData != null && shippingStatusData.getState() != null) {
			ShipmentEntity shipment =
					shipmentRepo
					.findByShippingServiceIdAndExternalIdAndOrganizationId(
							shippingStatusData.getServiceId()
							, shippingStatusData.getExternalShipmentId()
							, shippingStatusData.getOrgId())
							.orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, SHP$SRV$0009));
			shipment = updateShipmentStatus(shippingStatusData, shipment);
			updateOrderStatus(shippingStatusData, shipment.getSubOrder());
			logger.info("updated shipment status for service[{}] and org[{}] with params[{}]", serviceId, orgId, params);
		} else {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$PARS$0001);
		}
    }


    private void updateOrderStatus(ShipmentStatusData shippingStatusData, OrdersEntity subOrder) {
		ofNullable(shippingStatusData)
			.map(ShipmentStatusData::getState)
			.map(shippingStatusToOrderStatusMapping::get)
			.ifPresent(status -> orderService.updateOrderStatus(subOrder, status));
	}



	private ShipmentEntity updateShipmentStatus(ShipmentStatusData data, ShipmentEntity shipment) {
		shipment.setStatus(data.getState());
		return shipmentRepo.save(shipment);
	}


    private List<ShipmentItems> createShipmentItemsFromOrder(OrdersEntity subOrder) {
		return subOrder
				.getBasketsEntity()
				.stream()
				.map(this::createShipmentItem)
				.collect(toList());
	}




	private ShipmentItems createShipmentItem(BasketsEntity orderItem) {
		ShipmentItems shpItem = new ShipmentItems();

		Long stockId = getStockId(orderItem);
		String barcode = getBarcode(orderItem);
		String name = getProductName(orderItem);
		Integer quantity = getQuantity(orderItem);
		String specs = getVariantSpecs(orderItem);
		String productCode = getProductCode(orderItem);
		String sku = getSku(orderItem);
		BigDecimal weight = getVariantWeight(orderItem);

		shpItem.setStockId(stockId);
		shpItem.setBarcode(barcode);
		shpItem.setName(name);
		shpItem.setQuantity(quantity);
		shpItem.setSpecs(specs);
		shpItem.setProductCode(productCode);
		shpItem.setSku(sku);
		shpItem.setPrice(orderItem.getPrice());
		shpItem.setWeight(weight);

		return shpItem;
	}




	private ShipmentItems createShipmentItem(ReturnRequestItemEntity returnItem){
		ShipmentItems item = createShipmentItem(returnItem.getBasket());
		item.setReturnedItemId(returnItem.getId());
		return item;
	}






	private String getSku(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(ProductVariantsEntity::getSku)
				.orElse(null);
	}


	private BigDecimal getVariantWeight(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(ProductVariantsEntity::getWeight)
				.orElse(ZERO);
	}


	private String getProductCode(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(ProductVariantsEntity::getProductCode)
				.orElse(null);
	}



	private String getVariantSpecs(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(v -> productService.parseVariantFeatures(v, 0))
				.map(Map::toString)
				.orElse(null);
	}

	private Integer getQuantity(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getQuantity)
				.map(BigDecimal::intValue)
				.orElse(null);
	}



	private String getProductName(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(ProductVariantsEntity::getProductEntity)
				.map(ProductEntity::getName)
				.orElse(null);
	}



	private String getBarcode(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getProductVariantsEntity)
				.map(ProductVariantsEntity::getBarcode)
				.orElse(null);
	}



	private Long getStockId(BasketsEntity orderItem) {
		return ofNullable(orderItem)
				.map(BasketsEntity::getStocksEntity)
				.map(StocksEntity::getId)
				.orElse(null);
	}



	private ShipmentReceiver createShipmentReceiver(OrdersEntity order) {
		Long userId = order.getUserId();
		AddressesEntity addr = order.getAddressEntity();
		UserEntity customer = 
				userRepo
				.findById(userId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$USR$0001, userId));
		String name = customer.getName();
		String phone = getPhone(order, addr, customer);

		ShipmentReceiver receiver = new ShipmentReceiver();
		receiver.setEmail(customer.getEmail());
		receiver.setFirstName(name);
		receiver.setLastName(" ");
		receiver.setPhone(phone);
		return receiver;
	}



	private String getPhone(OrdersEntity order, AddressesEntity addr, UserEntity customer) {
		return 	firstExistingValueOf(
					ofNullable(addr.getPhoneNumber()).orElse(null)
					, customer.getMobile()
					, customer.getPhoneNumber())
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0003, order.getId()));
	}




	private Map<String,String> parseJsonAsMap(String json){
		try {
			return jsonMapper.readValue(json, new TypeReference<Map<String,String>>() {});
		} catch (IOException e) {
			logger.error(e,e);
			return emptyMap();
		}
	}



	@Override
	public List<ShippingServiceRegistration> listShippingServices() {
		Long orgId = securityService.getCurrentUserOrganizationId();
		List<OrganizationShippingServiceEntity> serviceEntities = 
				orgShippingServiceRepo.getByOrganization_Id(orgId);
		return serviceEntities
				.stream()
				.map(this::createShippingServiceRegistration)
				.collect(toList());
	}
	
	
	
	private ShippingServiceRegistration createShippingServiceRegistration(OrganizationShippingServiceEntity entity){
		String serviceId = entity.getServiceId();
		Map<String,Object> serviceParams = new HashMap<>();
		try {
			serviceParams = jsonMapper.readValue(entity.getServiceParameters(), new TypeReference<Map<String,Object>>(){});
		} catch (Throwable e) {
			logger.error(e, e);
		}
		ShippingServiceRegistration serviceReg = new ShippingServiceRegistration();
		serviceReg.setServiceId(serviceId);
		serviceReg.setServiceParameters(serviceParams);
		return serviceReg;
	}


	@Override
	public void unregisterFromShippingService(String serviceId) {
		OrganizationShippingServiceEntity orgService = getServiceParameters(serviceId);

		orgShippingServiceRepo.delete(orgService);
	}



	private OrganizationShippingServiceEntity getServiceParameters(String serviceId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		OrganizationShippingServiceEntity orgService =
				orgShippingServiceRepo
						.getByOrganization_IdAndServiceId(orgId, serviceId)
						.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0006, serviceId));
		return orgService;
	}



	@Override
	public Optional<String> getShippingServiceCartOptimizer(String shippingServiceId) {
		OrganizationShippingServiceEntity orgService = 
				getServiceParameters(shippingServiceId);
		List<ServiceParameter> parameters = parseServiceParameters(orgService);
		return parameters
				.stream()
				.filter(param -> Objects.equals(param.getParameter(), CART_OPTIMIZER.name()))
				.map(ServiceParameter::getValue)
				.findFirst();
	}



	@Override
	public Flux<ReturnShipmentTracker> requestReturnShipments(ReturnRequestEntity returnRequest) {
		Long orgId = securityService.getCurrentUserOrganizationId();

		List<ShippingDetails> shippingDetails = createShippingDetailsFromReturnRequest(returnRequest);
		String shippingServiceId = getShippingServiceId(returnRequest);
		return orgShippingServiceRepo
				.getByOrganization_IdAndServiceId(orgId, shippingServiceId)
				.flatMap(this::getShippingService)
				.map(service -> service.requestReturnShipment(shippingDetails))
				.map(trackersFlux -> createNewReturnShipmentsForReturnRequest(returnRequest, trackersFlux, shippingServiceId))
				.orElseGet(Flux::empty);
	}




	@Override
	public Optional<ShopsEntity> getPickupShop(String additionalDataJson, String shippingServiceId, Long orgId) {
		ShippingService shippingService = getShippingService(shippingServiceId, orgId);
		return shippingService
				.getPickupShop(additionalDataJson)
				.flatMap(shopRepo::findShopFullData);
	}



	@Override
	public Optional<ShopsEntity> getPickupShop(ShipmentEntity shipment) {
		Long orgId = shipment.getSubOrder().getMetaOrder().getOrganization().getId();
		return getPickupShop(shipment.getParameters(), shipment.getShippingServiceId(), orgId);
	}



	@Override
	public boolean isPickupService(String shippingServiceId) {
		return shippingServiceFactory
				.getServiceInfo(shippingServiceId)
				.map(ShippingServiceInfo::getType)
				.map(PICKUP::equals)
				.orElse(false);
	}



	@Override
	public Optional<ShippingServiceInfo> getShippingServiceInfo(String shippingServiceId) {
		return shippingServiceFactory
				.getServiceInfo(shippingServiceId);
	}

	@Override
	public OrderConfirmResponseDTO getShippingAirwayBill(Long orderId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		OrdersEntity order = orderRepo
				.findByIdAndOrganizationEntity_Id(orderId, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0004, orgId, orderId));
		String airwayBillNo = order.getShipment().getTrackNumber();
		return ofNullable(order.getShipment())
				.map(ShipmentEntity::getShippingServiceId)
				.flatMap(serviceId -> orgShippingServiceRepo.getByOrganization_IdAndServiceId(orgId, serviceId))
				.flatMap(this::getShippingService)
				.map(service -> service.getAirwayBill(airwayBillNo))
				.get()
				.blockOptional(Duration.ofSeconds(5))
				.map(file -> new OrderConfirmResponseDTO(file, "Airway Bill.pdf", "application/pdf"))
				.orElseGet(() -> new OrderConfirmResponseDTO());
	}

	@Override
	public String getTrackingUrl(Long orderId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		OrdersEntity order = orderRepo
				.findByIdAndOrganizationEntity_Id(orderId, orgId)
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, O$CFRM$0004, orgId, orderId));
		String airwayBillNo = order.getShipment().getTrackNumber();
		return ofNullable(order.getShipment())
				.map(ShipmentEntity::getShippingServiceId)
				.flatMap(serviceId -> orgShippingServiceRepo.getByOrganization_IdAndServiceId(orgId, serviceId))
				.flatMap(this::getShippingService)
				.map(service -> service.getTrackingUrl(airwayBillNo))
				.orElse("");
	}


	private Flux<ReturnShipmentTracker> createNewReturnShipmentsForReturnRequest(ReturnRequestEntity returnRequest
			, Flux<ReturnShipmentTracker> trackersFlux, String shippingServiceId) {
		return trackersFlux
				.doOnNext( tracker -> createReturnShipmentEntity(returnRequest, tracker, shippingServiceId));
	}



	private ReturnShipmentEntity createReturnShipmentEntity(ReturnRequestEntity returnRequest, ReturnShipmentTracker tracker, String shippingServiceId) {
		ReturnShipmentEntity returnShipment = new ReturnShipmentEntity();
		returnShipment.setExternalId(tracker.getShipmentExternalId());
		returnShipment.setShippingServiceId(shippingServiceId);
		returnShipment.setStatus(REQUSTED.getValue());
		returnShipment.setTrackNumber(tracker.getTracker());
		returnShipment = returnShipmentRepo.save(returnShipment);
		addReturnedItemsToReturnShipment(tracker, returnShipment);
		return returnShipmentRepo.save(returnShipment);
	}



	private void addReturnedItemsToReturnShipment(ReturnShipmentTracker tracker, ReturnShipmentEntity returnShipment) {
		tracker
			.getShippingDetails()
			.getItems()
			.stream()
			.map(ShipmentItems::getReturnedItemId)
			.collect(collectingAndThen(toList(), returnedItemRepo::findByIdIn))
			.forEach(returnShipment::addReturnItem);
	}


	private List<OrdersEntity> getReturnRequestSubOrders(ReturnRequestEntity returnRequest) {
		return returnRequest
				.getReturnedItems()
				.stream()
				.map(ReturnRequestItemEntity::getBasket)
				.map(BasketsEntity::getOrdersEntity)
				.collect(toList());
	}




	private String getShippingServiceId(ReturnRequestEntity returnRequest) {
		return getReturnRequestSubOrders(returnRequest)
				.stream()
				.map(OrdersEntity::getShipment)
				.map(ShipmentEntity::getShippingServiceId)
				.findFirst()
				.orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0004, returnRequest.getId()));
	}



	private List<ShippingDetails> createShippingDetailsFromReturnRequest(ReturnRequestEntity returnRequest) {
		return returnRequest
				.getReturnedItems()
				.stream()
				.collect(
						collectingAndThen(
							groupingBy(this::getOrderId)
							, itemsMap -> createShippingDetailsFromReturnRequestItems(returnRequest, itemsMap)));
	}

	
	
	
	private Long getOrderId(ReturnRequestItemEntity returnedItem) {
		return returnedItem.getBasket().getOrdersEntity().getId();
	}



	private List<ShippingDetails> createShippingDetailsFromReturnRequestItems(ReturnRequestEntity returnRequest
					, Map<Long, List<ReturnRequestItemEntity>> itemsMap) {
		return itemsMap
				.values()
				.stream()
				.map(items -> createShippingDetailsFromReturnedItems(returnRequest, items))
				.collect(toList());
	}




	private ShippingDetails createShippingDetailsFromReturnedItems(ReturnRequestEntity returnRequest
					, List<ReturnRequestItemEntity> returnedItems) {
		OrdersEntity subOrder =
				returnedItems
					.stream()
						.map(ReturnRequestItemEntity::getBasket)
						.map(BasketsEntity::getOrdersEntity)
						.findFirst()
						.orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0004, returnRequest.getId()));
		List<ShipmentItems> items =
				returnedItems
						.stream()
						.map(this::createShipmentItem)
						.collect(toList());

		ShippingDetails shippingData = new ShippingDetails();
		ShippingAddress customerAddr = createShippingAddress(subOrder.getAddressEntity());
		ShippingAddress shopAddr = createShippingAddress(subOrder.getShopsEntity().getAddressesEntity());
		ShipmentReceiver receiver = createReturnShipmentReceiver(subOrder);
		String callBackUrl = createCallBackUrl(subOrder);
		Long metaOrderId =
				ofNullable(subOrder)
						.map(OrdersEntity::getMetaOrder)
						.map(MetaOrderEntity::getId)
						.orElse(null);

		shippingData.setAdditionalData(emptyMap());
		shippingData.setDestination(shopAddr);
		shippingData.setReceiver(receiver);
		shippingData.setSource(customerAddr);
		shippingData.setItems(items);
		shippingData.setSubOrderId(subOrder.getId());
		shippingData.setMetaOrderId(metaOrderId);
		shippingData.setCallBackUrl(callBackUrl);
		shippingData.setShopId(subOrder.getShopsEntity().getId());
		shippingData.setReturnRequestId(returnRequest.getId());
		return shippingData;
	}



	private ShipmentReceiver createReturnShipmentReceiver(OrdersEntity order) {
		ShopsEntity shop = order.getShopsEntity();
		String phone =
				ofNullable(shop.getAddressesEntity())
				.map(AddressesEntity::getPhoneNumber)
				.orElse(shop.getPhoneNumber());

		ShipmentReceiver receiver = new ShipmentReceiver();
		receiver.setEmail(null);
		receiver.setFirstName(shop.getName());
		receiver.setLastName("...");
		receiver.setPhone(phone);
		return receiver;
	}

}




@Data
@AllArgsConstructor
class ShopAndItsAddress{
	private Long shopId;
	private Long addressId;
	
	
	public boolean anyIsNull() {
		return EntityUtils.anyIsNull(shopId, addressId);
	}
}


@Data
class ReturnShipmentData{
	private ReturnRequestEntity request;
	private String shippingServiceId;
	private String trackNumber;
	private String externalId;
}

