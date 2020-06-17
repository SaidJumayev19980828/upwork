package com.nasnav.shipping;

import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.google.common.collect.ImmutableMap;
import com.nasnav.shipping.services.TestShippingService;


/**
 * A singleton for providing shipping services by their ID.
 * */
//this is just used to fetch services, it doesn't have a dynamic state, so, i guess 
//it shouldn't cause problems with multi-threading
public final class ShippingServices {
	
	private List<ShippingService> activeShippingServices;
	private Map<String, ShippingService> services ;
	private static ShippingServices instance;
	
	

	
	/**
	 * add new shipping services here
	 * */
	private List<ShippingService> createShippingServicesList() {
		return asList( new TestShippingService());
	}
	
	
	
	
	
	
	
	private ShippingServices() {
		activeShippingServices = createShippingServicesList();
		
		services = ImmutableMap.copyOf(
						activeShippingServices
						.stream()
						.collect( 
							toMap(
								srv -> srv.getServiceInfo().getId()
								, srv -> srv)));
		instance = this;
	}


	
	private static ShippingServices getInstance() {
		return ofNullable(instance)
				.orElse(new ShippingServices());
	}
	
	
	private Map<String, ShippingService> getServices(){
		return services;
	}
	
	
	public static Optional<ShippingService> get(String id) {
		return ofNullable(getInstance().getServices().get(id));
	}
}
