package com.nasnav.shipping;

import static java.util.Arrays.asList;
import static java.util.Optional.empty;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_PROTOTYPE;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import javax.annotation.PostConstruct;

import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.GenericWebApplicationContext;

import com.nasnav.shipping.model.ServiceParameter;
import com.nasnav.shipping.model.ShippingServiceInfo;
import com.nasnav.shipping.services.DummyShippingService;
import com.nasnav.shipping.services.PickupFromShop;
import com.nasnav.shipping.services.PickupPointsWithInternalLogistics;
import com.nasnav.shipping.services.SallabShippingService;
import com.nasnav.shipping.services.bosta.BostaLevisShippingService;

import lombok.AllArgsConstructor;
import lombok.Data;


/**
 * A Factory service for providing shipping services by their ID.
 * */
@Service
public class ShippingServiceFactory {
	
	private static Logger logger = LogManager.getLogger(ShippingServiceFactory.class);
	
	/**
	 * add new shipping services here
	 * */
	private static final List<Class<? extends ShippingService>> activeShippingServices = 
			asList( DummyShippingService.class
					,BostaLevisShippingService.class
					,PickupFromShop.class
					,PickupPointsWithInternalLogistics.class
					,SallabShippingService.class);
	
	
	@Autowired
	private GenericWebApplicationContext context;
	
	
	
	@PostConstruct
	public void init() {
		activeShippingServices
		.stream()
		.map(this::getServiceId)
		.filter(Optional::isPresent)
		.map(Optional::get)
		.forEach(this::registerShippingServiceAsBean);
	}
	
	
	
	
	/**
	 * register the shipping services classes as spring beans with prototype scope.
	 * The main reason for this is to allow using other spring beans inside the shipping 
	 * services, allowing them to access other services and the database.
	 * 
	 * We do this here. to make the only step needed for adding a new shipping service is
	 * to add its class to ShippingServiceFactory.activeShippingServices.
	 * As shipping services needs to be created as prototypes with bean id = service id.
	 * Shipping services needs to be prototypes because they are stateful, they use the 
	 * provided ShippingServiceParameters provided to them after creation, 
	 * which are changed from organization to another.
	 * */
	private void registerShippingServiceAsBean(ServiceClassWithId serviceClass) {
		RootBeanDefinition beanDefinition = new RootBeanDefinition(serviceClass.getClass());
		beanDefinition.setInstanceSupplier(() -> createShippingServiceInstance(serviceClass));
		beanDefinition.setScope(SCOPE_PROTOTYPE);
		context.registerBeanDefinition(serviceClass.getId(), beanDefinition);
	}
	
	
	
	private Optional<ServiceClassWithId> getServiceId(Class<? extends ShippingService> srv) {
			return createShippingServiceInstance(srv)
					.map(ShippingService::getServiceInfo)
					.map(ShippingServiceInfo::getId)
					.map(id -> new ServiceClassWithId(srv, id));
	}

	
	

	
	public Optional<ShippingService> getShippingService(String id, List<ServiceParameter> serviceParameters) {
		return getShippingServiceBean(id)
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
	
	
	
	private Optional<ShippingService> getShippingServiceBean(String id){
		try {
			return ofNullable(context.getBean(id, ShippingService.class));
		}catch(Throwable t) {
			logger.error(t,t);
			return empty();
		}
	}
	
	
	
	private ShippingService createShippingServiceInstance(ServiceClassWithId serviceClassWithId) {
		Class<? extends ShippingService> srv = serviceClassWithId.getServiceClass();
		try {
			return srv.getDeclaredConstructor().newInstance();
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException | SecurityException e) {
			logger.error(e,e);
			throw new RuntimeException(e);
		}
	}
	
	
	
	public static List<ShippingServiceInfo> getAllServices(){
		return activeShippingServices
				.stream()
				.map(ShippingServiceFactory::createShippingServiceInstance)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ShippingService::getServiceInfo)
				.collect(toList());
	} 
	
	
	
	
	public static List<ShippingServiceInfo> getAllPublicServices(){
		return getAllServices()
				.stream()
				.filter(ShippingServiceInfo::isPublicService)
				.collect(toList());
	} 
	
	
	
	public Optional<ShippingServiceInfo> getServiceInfo(String serviceId) {
		return ofNullable(serviceId)
				.map(this::getShippingServiceBean)
				.filter(Optional::isPresent)
				.map(Optional::get)
				.map(ShippingService::getServiceInfo);
	}
	
}





@Data
@AllArgsConstructor
class ServiceClassWithId{
	private Class<? extends ShippingService> serviceClass;
	private String id;
}