package com.nasnav.shipping;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import com.google.common.collect.ImmutableMap;
import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.ShippingServiceInfo;
import com.nasnav.shipping.services.TestShippingService;
import com.nasnav.shipping.services.bosta.BostaLevisShippingService;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * A Factory class for providing shipping services by their ID.
 * */
//this is just used to fetch services, it doesn't have a dynamic state, so, i guess 
//it shouldn't cause problems with multi-threading
public final class ShippingServiceFactory {
	
	private static Logger logger = LogManager.getLogger(ShippingServiceFactory.class);
	
	/**
	 * add new shipping services here
	 * */
	private static final List<Class<? extends ShippingService>> activeShippingServices = 
			asList( TestShippingService.class
					,BostaLevisShippingService.class);
	
	
	private static Map<String, Class<? extends ShippingService>> services ;
	private static ShippingServiceFactory instance;
	
	
	
	
	
	
	
	private ShippingServiceFactory() {
		
		services = ImmutableMap.copyOf(
						activeShippingServices
						.stream()
						.map(this::getServiceId)
						.filter(Optional::isPresent)
						.map(Optional::get)
						.collect( 
							toMap(ServiceClassWithId::getId
								, ServiceClassWithId::getServiceClass)));
		instance = this;
	}
	
	
	
	
	
	private Optional<ServiceClassWithId> getServiceId(Class<? extends ShippingService> srv) {
			return createShippingServiceInstance(srv)
					.map(ShippingService::getServiceInfo)
					.map(ShippingServiceInfo::getId)
					.map(id -> new ServiceClassWithId(srv, id));
	}

	
	

	
	private static ShippingServiceFactory getInstance() {
		return ofNullable(instance)
				.orElse(new ShippingServiceFactory());
	}
	
	
	
	@SuppressWarnings("static-access")
	public static Optional<ShippingService> getShippingService(String id, List<ServiceParameter> serviceParameters) {
		return ofNullable(getInstance().services.get(id))
				.flatMap(ShippingServiceFactory::createShippingServiceInstance)
				.map(Stream::of)
				.orElseGet(Stream::empty)
				.peek(instance -> instance.setServiceParameters(serviceParameters))
				.findFirst();
	}
	
	
	
	private static Optional<ShippingService> createShippingServiceInstance(Class<? extends ShippingService> srv) {
		try {
			return ofNullable(srv.getDeclaredConstructor().newInstance());
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error(e,e);
			return empty();
		}
	}
}





@Data
@AllArgsConstructor
class ServiceClassWithId{
	private Class<? extends ShippingService> serviceClass;
	private String id;
}