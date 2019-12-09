package com.nasnav.integration.microsoftdynamics;

import static java.util.Optional.ofNullable;

import java.time.LocalDate;
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
		
		String serverUrl = integrationService.getIntegrationParamValue(ORG_ID, SERVER_URL_PARAM_NAME);
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
		
		String phone = ofNullable( dat.getPhone() ).orElse("");
		int gender = ofNullable( dat.getGender() ).orElse(1);
		String firstName = ofNullable( dat.getFirstName() ).orElse("");
		String lastName = ofNullable( dat.getLastName() ).orElse("");
		LocalDate birthDate = ofNullable( dat.getBirthDate() ).orElse(LocalDate.of(1990, 1, 1));
		
		Customer customer = new Customer();		
		customer.setBirthDate(birthDate);
		customer.setGender(gender);
		customer.setEmail(dat.getEmail());
		customer.setFirstName( firstName );		
		customer.setLastName( lastName );		
		customer.setPhoneNumber( phone );
		customer.setAddresses( addresses );
		customer.setMiddleName("");
		
		return customer;
	}





	private List<Address> toMsDynamicsAddressList(CustomerData dat) {
		AddressData addressDat = dat.getAddress();
		
		if(addressDat == null) {
			return new ArrayList<>();
		}
		
		Address address = new Address();
		String city = ofNullable( addressDat.getCity() ).orElse("") ;
		String country = ofNullable( addressDat.getCountry() ).orElse("Egypt");
		String phone = ofNullable( dat.getPhone() ).orElse("");
		String street = ofNullable( addressDat.getAddress() ).orElse("");
		String state = city;
		String zip = ofNullable("").orElse("");
		
		address.setCity( city );
		address.setCountry( country);
		address.setPhoneNumber(phone);
		address.setStreet( street);
		address.setState(state);
		address.setZipCode(zip);
		
		return Arrays.asList(address);
	}
	
	
	
	

	@Override
	protected CustomerCreateEvent handleError(CustomerCreateEvent event, Throwable t) {
		// TODO Auto-generated method stub
		return null;
	}

}
