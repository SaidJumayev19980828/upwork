package com.nasnav.service;

import static com.nasnav.exceptions.ErrorCodes.*;
import static com.nasnav.shipping.model.ParameterType.STRING;
import static java.math.BigDecimal.ZERO;
import static java.util.Collections.emptyList;
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

import com.nasnav.dto.request.cart.CartCheckoutDTO;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.OrganizationShippingServiceRepository;
import com.nasnav.dao.StockRepository;
import com.nasnav.dto.request.shipping.ShipmentDTO;
import com.nasnav.dto.request.shipping.ShippingAdditionalDataDTO;
import com.nasnav.dto.request.shipping.ShippingEtaDTO;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.CountriesEntity;
import com.nasnav.persistence.OrganizationShippingServiceEntity;
import com.nasnav.persistence.StocksEntity;
import com.nasnav.persistence.dto.query.result.CartItemShippingData;
import com.nasnav.shipping.ShippingService;
import com.nasnav.shipping.ShippingServiceFactory;
import com.nasnav.shipping.model.Parameter;
import com.nasnav.shipping.model.ParameterType;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.Shipment;
import com.nasnav.shipping.model.ShipmentItems;
import com.nasnav.shipping.model.ShippingAddress;
import com.nasnav.shipping.model.ShippingDetails;
import com.nasnav.shipping.model.ShippingOffer;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.Flux;

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
	
	
	@Override
	public List<ShippingOfferDTO> getShippingOffers(Long customerAddrId) {
		List<ShippingDetails> shippingDetails = createShippingDetailsFromCart(customerAddrId);
		
		return getOffersFromOrganizationShippingServices(shippingDetails);
	}



	@Override
	public void validateCartCheckoutAdditionalData(CartCheckoutDTO dto) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		OrganizationShippingServiceEntity orgShippingSvc =
				ofNullable(orgShippingServiceRepo.getByOrganization_IdAndServiceId(orgId, dto.getServiceId()))
				.orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SHIP$0001));


		Optional<ShippingService> shippingService = getShippingService(orgShippingSvc);
		if (!shippingService.isPresent()) {
			throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, SHP$SVC$0001);
		}

		List<ShippingDetails> shippingDetails = createShippingDetailsFromCart(dto.getAddressId());
		for(ShippingDetails shippingDetail : shippingDetails) {
			shippingDetail.setAdditionalData(dto.getAdditionalData());
		}

		shippingService.get().validateShipment(shippingDetails);

	}



	private List<ShippingOfferDTO> getOffersFromOrganizationShippingServices(List<ShippingDetails> shippingDetails) {
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
	
	
	
	
	
	private Optional<ShippingService> getShippingService(OrganizationShippingServiceEntity orgShippingService){
		String id = orgShippingService.getServiceId();
		List<ServiceParameter> serviceParameters = parseServiceParameters(orgShippingService);
		
		return ShippingServiceFactory.getShippingService(id, serviceParameters);
	}

	
	
	

	private List<ServiceParameter> parseServiceParameters(OrganizationShippingServiceEntity orgShippingService) {
		String serviceParamsString = ofNullable(orgShippingService.getServiceParameters()).orElse("{}");
		
		ObjectMapper mapper = new ObjectMapper();
		TypeReference<HashMap<String, String>> typeRef = new TypeReference<HashMap<String, String>>() {};
		Map<String, String> paramMap = new HashMap<>();
		try {
			paramMap = mapper.readValue(serviceParamsString, typeRef) ;
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
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, ADDR$ADDR$0002, customerAddrId));
		
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
