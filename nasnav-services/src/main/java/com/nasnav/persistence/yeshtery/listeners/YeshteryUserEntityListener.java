package com.nasnav.persistence.yeshtery.listeners;

import com.nasnav.integration.IntegrationServiceAdapter;
import com.nasnav.integration.events.data.AddressData;
import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.persistence.AddressesEntity;
import com.nasnav.persistence.yeshtery.YeshteryUserEntity;
import com.sun.istack.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import javax.persistence.PostPersist;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Component
public class YeshteryUserEntityListener {
	
	private static IntegrationServiceAdapter integrationHelper;
	private static Logger logger = Logger.getLogger(YeshteryUserEntityListener.class);
	
	
	@Autowired
	public void setIntegrationServiceHelper(IntegrationServiceAdapter integrationHelper) {
		YeshteryUserEntityListener.integrationHelper = integrationHelper;
	}

	@PostPersist
	public void postPresist(YeshteryUserEntity user) {
		TransactionSynchronizationManager
		.registerSynchronization( 
	            new TransactionSynchronizationAdapter() {
	                @Override
	                public void afterCommit() {
	                	doPostPersistLogic(user);}
	            });
		
	}

	private void doPostPersistLogic(YeshteryUserEntity user) {
		CustomerData customer = createCustomerData(user);		
		Long orgId = user.getOrganizationId();		
		
		integrationHelper.pushCustomerCreationEvent(customer, orgId);
	}

	private CustomerData createCustomerData(YeshteryUserEntity user) {
		AddressData address = createAddressData(user);
		
		CustomerData customer = new CustomerData();
		customer.setAddress(address);
		customer.setEmail( user.getEmail() );
		customer.setFirstName( user.getName());
		customer.setLastName( "" );
		customer.setPhone( user.getPhoneNumber() );
		customer.setId( user.getId() );
		
		return customer;
	}

	private AddressData createAddressData(YeshteryUserEntity user) {
		List<AddressesEntity> userAddresses = user.getAddresses().stream()
												 .filter(Objects::nonNull)
												 .collect(Collectors.toList());

		AddressData address = new AddressData();
		if (!userAddresses.isEmpty()) {
			AddressesEntity userAddress = userAddresses.get(0);
			address.setAddress(userAddress.getAddressLine1());
			if (userAddress.getAreasEntity() != null && userAddress.getAreasEntity().getCitiesEntity() != null) {
				address.setCity(userAddress.getAreasEntity().getCitiesEntity().getName());
				if (userAddress.getAreasEntity().getCitiesEntity().getCountriesEntity() != null) {
					address.setCountry(userAddress.getAreasEntity().getCitiesEntity().getCountriesEntity().getName());
				}
			}
		}
		return address;
	}
	
}
