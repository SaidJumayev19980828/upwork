package com.nasnav.integration.microsoftdynamics;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.ClientResponse;

import com.nasnav.integration.IntegrationEventListener;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.events.CustomerCreateEvent;
import com.nasnav.integration.events.EventInfo;
import com.nasnav.integration.events.data.AddressData;
import com.nasnav.integration.events.data.CustomerData;
import com.nasnav.integration.microsoftdynamics.webclient.FortuneWebClient;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Address;
import com.nasnav.integration.microsoftdynamics.webclient.dto.Customer;

import reactor.core.publisher.Mono;

public class CustomerCreateEventListener extends IntegrationEventListener<CustomerCreateEvent, CustomerData, String> {
	
	private static final String SERVER_URL_PARAM_NAME = "SERVER_URL";
	private FortuneWebClient client;
	public static Long ORG_ID = 99001L;
	
	
	public CustomerCreateEventListener(IntegrationService integrationService) {
		super(integrationService);
		
		String serverUrl = this.integrationService.getIntegrationParamValue(ORG_ID, SERVER_URL_PARAM_NAME);
		client = new FortuneWebClient(serverUrl);
	}
	
	
	
	

	@Override
	protected Mono<String> handleEventAsync(EventInfo<CustomerData> event) {
		Customer customer = toMsDynamicsCustomer( event.getEventData() );		
		return client.createCustomer(customer)
					.filter( res -> res.statusCode() == HttpStatus.OK)
					.doOnSuccess(this::throwExceptionIfNotOk)
					.flatMap(res -> res.bodyToMono(String.class));
		
	}
	
	
	
	
	private void throwExceptionIfNotOk(ClientResponse response) {
		if(response.statusCode() != HttpStatus.OK) {
			throw new RuntimeException("Failed to get valid response from Microsoft Dynamics API ");
		}
	}





	private Customer toMsDynamicsCustomer(CustomerData dat) {
		List<Address> addresses = toMsDynamicsAddressList(dat);
		
		Customer customer = new Customer();		
		customer.setBirthDate(dat.getBirthDate());
		customer.setEmail(dat.getEmail());
		customer.setFirstName(dat.getFirstName());
		customer.setGender(dat.getGender());
		customer.setLastName(dat.getLastName());
		customer.setMiddleName("");		
		customer.setAddresses(addresses);
		
		return customer;
	}





	private List<Address> toMsDynamicsAddressList(CustomerData dat) {
		AddressData addressDat = dat.getAddress();
		
		if(addressDat == null) {
			return new ArrayList<>();
		}
		
		Address address = new Address();		
		address.setCity(addressDat.getCity());
		address.setCountry(addressDat.getCountry());
		address.setPhoneNumber(dat.getPhone());
		address.setStreet(addressDat.getAddress());
		List<Address> addresses = Arrays.asList(address);
		
		return addresses;
	}
	
	
	
	

	@Override
	protected CustomerCreateEvent handleError(CustomerCreateEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}

}
