package com.nasnav.service;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dao.CartItemRepository;
import com.nasnav.dao.OrganizationShippingServiceRepository;
import com.nasnav.dto.request.shipping.ShippingOfferDTO;
import com.nasnav.shipping.model.ShippingDetails;

@Service
public class ShippingManagementServiceImpl implements ShippingManagementService {

	
	@Autowired
	OrganizationShippingServiceRepository orgShippingServiceRepo;
	
	@Autowired
	CartItemRepository cartRepo;
	
	@Autowired
	SecurityService securityService;
	
	
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
		//get cart (stockId, shopId, addressId)
		//get shop addresses 
		//get user address
		//group cart by shopId
		//for each shop create shipping Detail
		return null;
	}
	
}
