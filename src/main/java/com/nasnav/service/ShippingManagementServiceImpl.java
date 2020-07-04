package com.nasnav.service;

import static com.nasnav.commons.utils.EntityUtils.firstExistingValueOf;
import static com.nasnav.exceptions.ErrorCodes.ADDR$ADDR$0002;
import static com.nasnav.exceptions.ErrorCodes.O$CFRM$0003;
import static com.nasnav.exceptions.ErrorCodes.O$SHP$0001;
import static com.nasnav.exceptions.ErrorCodes.ORG$SHIP$0001;
import static com.nasnav.exceptions.ErrorCodes.S$0004;
import static com.nasnav.exceptions.ErrorCodes.SHP$OFFR$0001;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0003;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0006;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0007;
import static com.nasnav.exceptions.ErrorCodes.SHP$SRV$0008;
import static com.nasnav.exceptions.ErrorCodes.SHP$SVC$0001;
import static com.nasnav.exceptions.ErrorCodes.SHP$USR$0001;
import static com.nasnav.shipping.ShippingServiceFactory.getServiceInfo;
import static com.nasnav.shipping.model.ParameterType.STRING;
import static java.math.BigDecimal.ZERO;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.emptyMap;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

import javax.transaction.Transactional;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.OrganizationShippingServiceRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dao.UserRepository;
import com.nasnav.dto.request.cart.CartCheckoutDTO;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingAdditionalDataDTO;
import com.nasnav.dto.request.shipping.ShippingEtaDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.dto.request.shipping.ShippingServiceRegistration;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.CountriesEntity;
import com.nasnav.persistence.OrdersEntity;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.OrganizationShippingServiceEntity;
import com.nasnav.persistence.ShipmentEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.persistence.dto.query.result.CartItemShippingData;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.Parameter;
import com.nasnav.shipping.model.ParameterType;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.Shipment;
import com.nasnav.shipping.model.ShipmentItems;
import com.nasnav.shipping.model.ShipmentReceiver;
import com.nasnav.shipping.model.ShipmentTracker;
import com.nasnav.shipping.model.ShippingAddress;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingOffer;
import com.nasnav.shipping.model.ShippingServiceInfo;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Service
public class ShippingManagementServiceImpl implements ShippingManagementService {
	
	private Logger logger = LogManager.getLogger(getClass());
	
	@Autowired
	private OrganizationShippingServiceRepository orgShippingServiceRepo;
	
	@Autowired
	private CartItemRepository cartRepo;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private AddressRepository addressRepo;
	
	@Autowired
	private StockRepository stockRepo;
	
	@Autowired
	private ObjectMapper jsonMapper;
	
	@Autowired
	private UserRepository userRepo;
	
	@Override
	public List<ShippingOfferDTO> getShippingOffers(Long customerAddrId) {
		List<ShippingDetails> shippingDetails = createShippingDetailsFromCart(customerAddrId);
		
		return getOffersFromOrganizationShippingServices(shippingDetails);
	}



	@Override
	public void validateShippingAdditionalData(CartCheckoutDTO dto) {
		ShippingService shippingService = getShippingService(dto.getServiceId());

		List<ShippingDetails> shippingDetails = createShippingDetailsFromCart(dto.getAddressId());
		for(ShippingDetails shippingDetail : shippingDetails) {
			shippingDetail.setAdditionalData(dto.getAdditionalData());
		}

		shippingService.validateShipment(shippingDetails);
	}


	
	
	
	private ShippingService getShippingService(String serviceId) {
		Long orgId = securityService.getCurrentUserOrganizationId();

		Optional<ShippingService> shippingService =
					orgShippingServiceRepo
					.getByOrganization_IdAndServiceId(orgId, serviceId)
					.map(this::getShippingService)
					.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SHIP$0001));

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
	
	
	
	
	private ShippingOfferDTO createShippingOfferDTO(ShippingOffer data) {
		ShippingOfferDTO offerDto = new ShippingOfferDTO();
		List<ShippingAdditionalDataDTO> additionalParams = getAdditionalParametersDtoList(data);
		List<ShipmentDTO> shipments = getShipmentDtoList(data);
		BigDecimal total = calculateTotal(shipments);
		offerDto.setAdditionalData(additionalParams);
		offerDto.setServiceId(data.getService().getId());
		offerDto.setServiceName(data.getService().getName());
		offerDto.setShipments(shipments);
		offerDto.setTotal(total);
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
				.collect(toList());
	}


	
	
	private ShipmentDTO createShipmentDTO(Shipment shipment) {
		ShippingEtaDTO etaDto = 
				ofNullable(shipment.getEta())
				.map(eta -> new ShippingEtaDTO(eta.getFrom(), eta.getTo()))
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
		return new ShippingAdditionalDataDTO(param.getName(), type);
	}
	
	
	
	
	@Override
	public Optional<ShippingService> getShippingService(OrganizationShippingServiceEntity orgShippingService){
		String id = orgShippingService.getServiceId();
		List<ServiceParameter> serviceParameters = parseServiceParameters(orgShippingService);
		
		return ShippingServiceFactory.getShippingService(id, serviceParameters);
	}

	
	
	

	public List<ServiceParameter> parseServiceParameters(OrganizationShippingServiceEntity orgShippingService) {
		String serviceParamsString = ofNullable(orgShippingService.getServiceParameters()).orElse("{}");
		
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
		Map<String, String> paramMap = new HashMap<>();
		try {
			paramMap = jsonMapper.readValue(serviceParamsString, typeRef) ;
		} catch (IOException e) {
			logger.error(e,e);
		}
		return paramMap
				.entrySet()
				.stream()
				.map(e -> new ServiceParameter(e.getKey(), e.getValue()))
				.collect(toList());
	}

	
	
	
	private List<ShippingDetails> createShippingDetailsFromCart(Long customerAddrId) {
		Long userId = securityService.getCurrentUser().getId();
		List<CartItemShippingData> cartData = cartRepo.findCartItemsShippingDataByUser_Id(userId);
		Map<Long, AddressesEntity> addresses = getAddresses(customerAddrId, cartData);
		
		validateCartItemShops(cartData);
		
		return cartData
				.stream()
				.collect(groupingBy(CartItemShippingData::getShopAddressId))
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


	
	
	private ShippingDetails createShippingDetails(Map.Entry<Long, List<CartItemShippingData>> entry
			, Map<Long, AddressesEntity> addresses, Long customerAddrId) {
		Long shopAddressId = entry.getKey();
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
		return shippingDetails;
	}
	
	
	
	
	
	private ShippingAddress createShippingAddress(AddressesEntity entity) {
		Optional<AreasEntity> area = ofNullable(entity).map(AddressesEntity::getAreasEntity);
		Optional<CitiesEntity> city = area.map(AreasEntity::getCitiesEntity);
		Optional<CountriesEntity> country = city.map(CitiesEntity::getCountriesEntity);
		
		Long areaId = area.map(AreasEntity::getId).orElse(-1L);
		Long cityId = city.map(CitiesEntity::getId).orElse(-1L);
		Long countryId = country.map(CountriesEntity::getId).orElse(-1L);
		
		BaseUserEntity user = securityService.getCurrentUser();
		
		ShippingAddress addr = new ShippingAddress();
		addr.setAddressLine1(entity.getAddressLine1());
		addr.setAddressLine2(entity.getAddressLine2());
		addr.setArea(areaId);
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
		return new ShipmentItems(data.getStockId());
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
				getServiceInfo(serviceId)
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
		try {
			Object paramVal = ofNullable(paramsJson.get(param.getName())).orElse("null");
			if(isNull(paramVal)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0003, param.getName(), serviceId);
			}
			if(!param.getType().getJavaType().isInstance(paramVal)) {
				throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0008, paramVal.toString(), param.getName(), serviceId);
			}
		}catch(JSONException e) {
			throw new RuntimeBusinessException(NOT_ACCEPTABLE, SHP$SRV$0003, param.getName(), serviceId);
		}
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
				.map(trackerFlux -> trackerFlux.singleOrEmpty())
				.orElseThrow( () -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, O$SHP$0001, subOrder.getId()));
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
		
		shippingData.setAdditionalData(additionalParameters);
		shippingData.setDestination(customerAddr);
		shippingData.setReceiver(receiver);
		shippingData.setSource(shopAddr);
		shippingData.setItems(items);
		shippingData.setSubOrderId(subOrder.getId());
		return shippingData;
	}




	private List<ShipmentItems> createShipmentItemsFromOrder(OrdersEntity subOrder) {
		return subOrder
		.getBasketsEntity()
		.stream()
		.map(BasketsEntity::getStocksEntity)
		.map(StocksEntity::getId)
		.map(ShipmentItems::new)
		.collect(toList());
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
