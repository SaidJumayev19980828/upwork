package com.nasnav.persistence.listeners;

import javax.persistence.PostPersist;

import com.nasnav.persistence.AddressesEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.nasnav.integration.IntegrationServiceAdapter;
import com.nasnav.integration.events.data.AddressData;
import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.persistence.UserEntity;
import com.sun.istack.logging.Logger;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;


@Component
public class UserEntityListener {
	
	private static IntegrationServiceAdapter integrationHelper;
	private static Logger logger = Logger.getLogger(UserEntityListener.class);
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceAdapter integrationHelper) {
		UserEntityListener.integrationHelper = integrationHelper;
	}
	
	
	
	
	
	@PostPersist
	public void postPresist(UserEntity user) {		
		CustomerData customer = createCustomerData(user);		
		Long orgId = user.getOrganizationId();		
		
		integrationHelper.pushCustomerCreationEvent(customer, orgId);
	}





	private CustomerData createCustomerData(UserEntity user) {
		AddressData address = createAddressData(user);
		
		CustomerData customer = new CustomerData();
		customer.setAddress(address);
		customer.setEmail( user.getEmail() );
		customer.setFirstName( user.getName());
		customer.setLastName( "" );
		customer.setPhone( user.getPhoneNumber() );
		customer.setId( user.getId() );
		
		//TODO nasnav users needs to have those fields
//		customer.setBirthDate( LocalDate.of(1980, 1, 1));
//		customer.setGender(1);
		
		return customer;
	}





	private AddressData createAddressData(UserEntity user) {
		List<AddressesEntity> userAddresses = user.getAddresses().stream()
												 .filter(Objects::nonNull)
												 .collect(Collectors.toList());

		AddressData address = new AddressData();
		if (!userAddresses.isEmpty()) {
			AddressesEntity userAddress = userAddresses.get(0);
			address.setAddress(userAddress.getAddressLine1());
			address.setCity(userAddress.getAreasEntity().getCitiesEntity().getName());
			address.setCountry(userAddress.getAreasEntity().getCitiesEntity().getCountriesEntity().getName());
		}
		return address;
	}
	
}
