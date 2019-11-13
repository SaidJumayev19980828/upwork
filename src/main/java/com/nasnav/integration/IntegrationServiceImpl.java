package com.nasnav.integration;

import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_EVENT_HANDLE_GENERAL_ERROR;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_MODULE_LOAD_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_LOADING_INTEGRATION_MODULE_CLASS;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_MISSING_MANDATORY_PARAMS;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_NO_INTEGRATION_MODULE;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_NO_INTEGRATION_PARAMS;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.IntegrationEventFailureRepository;
import com.nasnav.dao.IntegrationParamRepository;
import com.nasnav.dao.IntegrationParamTypeRepostory;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.IntegrationParam;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.integration.model.IntegratedShop;
import com.nasnav.integration.model.OrganizationIntegrationInfo;
import com.nasnav.persistence.IntegrationEventFailureEntity;
import com.nasnav.persistence.IntegrationParamEntity;
import com.nasnav.persistence.IntegrationParamTypeEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import reactor.core.publisher.EmitterProcessor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.FluxSink;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Service
public class IntegrationServiceImpl implements IntegrationService {
	Logger logger = Logger.getLogger(getClass());
	
	
	@Autowired
	IntegrationParamRepository paramRepo;
	
	@Autowired
	IntegrationParamTypeRepostory paramTypeRepo;
	
	
	private Map<Long, OrganizationIntegrationInfo> orgIntegration;
	
	private Set<IntegrationParamTypeEntity> mandatoryIntegrationParams;
	
	private FluxSink<EventHandling> eventFluxSink;
	private Flux<Mono<EventHandling>> eventFlux;
	
	@Autowired
	private IntegrationEventFailureRepository eventFailureRepo;
	
	
	@PostConstruct
	public void init() throws BusinessException {
		orgIntegration = new HashMap<>();
		mandatoryIntegrationParams = getMandatoryParams();
		loadIntegrationModules();
		initEventFlux();
	}
	
	
	
	

	
	private void initEventFlux() {
		Scheduler scheduler = Schedulers.boundedElastic(); 
		EmitterProcessor<EventHandling> emitterProcessor = EmitterProcessor.create();
		eventFlux =
				emitterProcessor									
						.publishOn(scheduler)
						.sample(Duration.ofMillis(100L))
						.map(Mono::just)						
						.publish()
						.autoConnect();						
							
		
		eventFluxSink = emitterProcessor.sink();
		eventFlux.subscribe(m -> m.subscribeOn(scheduler)
									.subscribe(this::handle));
	}


	
	


	private Set<IntegrationParamTypeEntity> getMandatoryParams() {		
		return paramTypeRepo.findByIsMandatory(true);
	}




	@Override
	public void setIntegrationModule(Long orgId, String classFullName) {
		// TODO Auto-generated method stub

	}
	
	
	
	

	@Override
	public IntegrationModule getIntegrationModule(Long orgId) {
		return orgIntegration.get(orgId).getIntegrationModule();
	}
	
	
	
	

	@Override
	public void loadIntegrationModules() throws BusinessException {	
		List<Long> integratedOrgs = getIntegratedOrganizations();
		for(Long orgId: integratedOrgs) {
			try {
				OrganizationIntegrationInfo integration = getOrganizationIntegrationInfo(orgId);
				this.orgIntegration.put(orgId, integration);
			}
			catch(Exception e) {
				String msg = String.format(ERR_INTEGRATION_MODULE_LOAD_FAILED, orgId);
				logger.error(msg, e);
				throwIntegrationInitException(msg);
			}
		}
	}



	

	private List<Long> getIntegratedOrganizations() {
		String paramName = IntegrationParam.INTEGRATION_MODULE.getValue();
		return paramRepo.findByType_typeName( paramName ) 
						.stream()
						.map(IntegrationParamEntity::getOrganizationId)
						.collect(Collectors.toList());
	}
	
	
	
	
	
	
	private OrganizationIntegrationInfo getOrganizationIntegrationInfo(Long orgId) throws BusinessException {
						
		List<IntegrationParamEntity> params = getOrgInegrationParams(orgId);
		IntegrationModule integrationModule = loadIntegrationModule(orgId, params);
		
		return new OrganizationIntegrationInfo(integrationModule, params);
	}




	private IntegrationModule loadIntegrationModule(Long orgId, List<IntegrationParamEntity> params)
			throws BusinessException {
		IntegrationModule integrationModule = null;
		String integrationModuleClass = getIntegrationModuleClassName(params);
		
		try {
			@SuppressWarnings("unchecked")
			Class<IntegrationModule> moduleClass = (Class<IntegrationModule>) this.getClass().getClassLoader().loadClass(integrationModuleClass);
			integrationModule = moduleClass.getDeclaredConstructor(IntegrationService.class).newInstance(this);						
		} catch (Exception e) {
			logger.error(e,e);
			throwIntegrationInitException(ERR_LOADING_INTEGRATION_MODULE_CLASS, integrationModuleClass, orgId);
		}
		return integrationModule;
	}




	
	
	private String getIntegrationModuleClassName(List<IntegrationParamEntity> params) throws BusinessException {
		String integrationModuleClass = params.stream()
												.filter(this::isIntegrationModuleNameParam)
												.map(IntegrationParamEntity::getParamValue)
												.findFirst()
												.orElseThrow(() -> getIntegrationInitException(ERR_NO_INTEGRATION_MODULE));
		return integrationModuleClass;
	}
	
	




	private boolean isIntegrationModuleNameParam(IntegrationParamEntity p) {
		return Objects.equals(p.getType().getTypeName(), IntegrationParam.INTEGRATION_MODULE.getValue() );
	}




	private List<IntegrationParamEntity> getOrgInegrationParams(Long orgId) throws BusinessException {
		List<IntegrationParamEntity> params = paramRepo.findByOrganizationId(orgId);
				
		validateLoadedIntegrationParams(orgId, params);
		return params;
	}
	
	
	
	
	
	

	private void validateLoadedIntegrationParams(Long orgId, List<IntegrationParamEntity> params) throws BusinessException {
		if(params == null || params.isEmpty()) {
			throwIntegrationInitException(ERR_NO_INTEGRATION_PARAMS, orgId);
		}		
		
		if(!hasMandatoryParams(params)) {
			throwIntegrationInitException(ERR_MISSING_MANDATORY_PARAMS, orgId);
		}
	}



	

	private void throwIntegrationInitException(String msg, Object... args) throws BusinessException {
		throw getIntegrationInitException(msg, args);
	}

	
	
	
	
	private BusinessException getIntegrationInitException(String msg, Object... args) {
		return new BusinessException( String.format( msg, args )
										, "INTEGRATION INITIALIZATION FAILURE"
										, HttpStatus.INTERNAL_SERVER_ERROR);
	}

	



	private Boolean hasMandatoryParams(List<IntegrationParamEntity> params) {
		Boolean mandatoryParamsExists = params.stream()
											.map(IntegrationParamEntity::getType)
											.collect(Collectors.toSet())
											.containsAll(mandatoryIntegrationParams);
		return mandatoryParamsExists;
	}


	


	@Override
	public void addMappedValue(Long orgId, MappingType type, String localValue, String remoteValue) {
		// TODO Auto-generated method stub

	}
	
	
	
	

	@Override
	public String getExternalMappedValue(Long orgId, MappingType type, String localValue) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public String getLocalMappedValue(Long orgId, MappingType type, String externalValue) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public List<IntegratedShop> fetchOrganizationShops(Long orgId) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public void mapToIntegratedShop(Long shopId, IntegratedShop integratedShop) {
		// TODO Auto-generated method stub

	}
	
	
	
	
	

	@Override
	public void importOrganizationProducts(Long orgId, Runnable onComplete, Runnable onError) {
		// TODO Auto-generated method stub

	}
	
	
	
	

	@Override
	public BigDecimal getExternalStock(Long localStockId, Runnable onComplete, Runnable onError) {
		// TODO Auto-generated method stub
		return null;
	}
	
	
	
	
	

	@Override
	public <E extends Event<T,R>, T, R> void pushIntegrationEvent(E event, Consumer<E> onComplete, BiConsumer<E, Throwable> onError) throws InvalidIntegrationEventException {
		validateEvent(event);
		try {			
			eventFluxSink.next( EventHandling.of(event, onComplete, onError) );			
		}catch(Throwable e) {
			logger.error(e);
			runErrorCallback(event, e, onError);
		}		
	}
	
	
	
	
	
	private <E extends Event<T,R>, T, R> void runErrorCallback(E event, Throwable handlingException, BiConsumer<E, Throwable> callback){
		try {
			callback.accept(event, handlingException);
		}catch(Throwable t) {
			logger.error(t);
			runGeneralErrorFallback(event, handlingException, t);
		}		
	}
	
	
	
	
	
	
	private <E extends Event<T,R>, T, R> void handle(EventHandling<E,T,R> handling) {
		E event = handling.getEvent();
		OrganizationIntegrationInfo integration =  orgIntegration.get(event.getOrganizationId());
		if(integration == null) {			
			return; 	//ignore events if its organization has no integration info. loaded.
		}
		
		integration.getIntegrationModule()
					.pushEvent(handling);		
	}



	

	private <T,R> void validateEvent(Event<T,R> event) throws InvalidIntegrationEventException {
		if(event == null || event.getOrganizationId() == null ) {
			throw new InvalidIntegrationEventException(
					String.format("Invalid event [%s]", event)
					, "INVALID INTEGRATION EVENT"
					, HttpStatus.INTERNAL_SERVER_ERROR);
		}
	}






	@Override
	public <E extends Event<T, R>, T, R> void runGeneralErrorFallback(E event, Throwable handlingException, Throwable fallbackException) {
		logger.error(String.format(ERR_EVENT_HANDLE_GENERAL_ERROR
											, event
											, handlingException.getClass()
											, fallbackException.getClass())	);
		
		saveEventFailureToDB(event, handlingException, fallbackException);
	}






	public <E extends Event<T, R> ,T,R> void saveEventFailureToDB(E event, Throwable handlingException,
			Throwable fallbackException) {
						
		ObjectMapper mapper = new ObjectMapper();
		String eventData;
		try {
			eventData = mapper.writeValueAsString(event);
		} catch (JsonProcessingException e) {
			eventData = event.toString();
		}
		
		IntegrationEventFailureEntity eventFailure = new IntegrationEventFailureEntity();
		eventFailure.setEventType( event.getClass().getName() );
		eventFailure.setEventData(eventData);
		eventFailure.setOrganizationId( event.getOrganizationId() );
		eventFailure.setHandleException( stackTraceToString(handlingException) );
		eventFailure.setFallbackException( stackTraceToString(fallbackException) );
		eventFailureRepo.save(eventFailure);
	}
	
	
	
	
	private String stackTraceToString(Throwable t) {
		StringWriter sw = new StringWriter();
		PrintWriter pw = new PrintWriter(sw);
		t.printStackTrace(pw);
		
		return sw.toString(); 
	}

}



@Data
@AllArgsConstructor
class EventHandling<E extends Event<T,R>, T, R>{
	E event;
	Consumer<E> onComplete;
	BiConsumer<E, Throwable> onError;
	
	
	public static <E extends Event<T,R>, T, R> EventHandling<E,T,R> of(E event, Consumer<E> onComplete, BiConsumer<E, Throwable> onError){
		return new EventHandling<E,T,R>(event, onComplete, onError);
	} 
}
