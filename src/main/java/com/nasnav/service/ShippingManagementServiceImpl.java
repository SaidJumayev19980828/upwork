package com.nasnav.service;

import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dao.AddressRepository;
import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.OrganizationShippingServiceRepository;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.AreasEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.CitiesEntity;
import com.nasnav.persistence.CountriesEntity;
import com.nasnav.persistence.dto.query.result.CartItemShippingData;
import com.nasnav.shipping.model.ShipmentItems;
import com.nasnav.shipping.model.ShippingAddress;
import com.nasnav.shipping.model.ShippingDetails;

@Service
public class ShippingManagementServiceImpl implements ShippingManagementService {

	
	@Autowired
	private OrganizationShippingServiceRepository orgShippingServiceRepo;
	
	@Autowired
	private CartItemRepository cartRepo;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private AddressRepository addressRepo;
	
	
	@Override
	public List<ShippingOfferDTO> getShippingOffers(Long customerAddrId) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		
		List<ShippingDetails> shippingDetails = createShippingDetailsFromCart(customerAddrId);
		
		
//		List<OrganizationShippingServiceEntity> shippingServiceParams = 
		Flux.fromIterable(orgShippingServiceRepo.getByOrganization_Id(orgId))
			.map(srvParam -> ShippingServices.get(srvParam.getServiceId()) )
			.filter(Optional::isPresent)
			.map()
			.createShippingOffer(shippingDetails) )
			.b;
		
				
		return null;
	}


	
	
	
	private List<ShippingDetails> createShippingDetailsFromCart(Long customerAddrId) {
		Long userId = securityService.getCurrentUser().getId();
		List<CartItemShippingData> cartData = cartRepo.findCartItemsShippingDataByUser_Id(userId);
		Map<Long, AddressesEntity> addresses = getAddresses(customerAddrId, cartData);
		
		return cartData
				.stream()
				.collect(groupingBy(CartItemShippingData::getShopId))
				.entrySet()
				.stream()
				.map(itemsPerShop -> createShippingDetails(itemsPerShop, addresses, customerAddrId))
				.collect(toList());
	}


	
	
	private ShippingDetails createShippingDetails(Map.Entry<Long, List<CartItemShippingData>> entry
			, Map<Long, AddressesEntity> addresses, Long customerAddrId) {
		Long shopId = entry.getKey();
		List<ShipmentItems> items = entry.getValue().stream().map(this::createShipmentItem).collect(toList());
		
		ShippingAddress pickupAddr = createShippingAddress( addresses.get(shopId) );
		ShippingAddress customerAddr = createShippingAddress( addresses.get(customerAddrId) ); 
		
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
		return new ShipmentItems(data.getShopId());
	}
	
	
	
	
	
	private Map<Long, AddressesEntity> getAddresses(Long customerAddrId, List<CartItemShippingData> cartData) {
		List<Long> addrIds = 
				cartData
				.stream()
				.map(CartItemShippingData::getShopAddressId)
				.collect(toList());
		addrIds.add(customerAddrId);
		 return	addressRepo
					.findByIdIn(addrIds)
					.stream()
					.collect(toMap(AddressesEntity::getId, addr -> addr));
	}
	
}
