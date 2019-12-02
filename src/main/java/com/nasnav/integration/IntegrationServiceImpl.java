package com.nasnav.integration;

import static com.nasnav.commons.utils.EntityUtils.failSafeFunction;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_EVENT_HANDLE_GENERAL_ERROR;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INTEGRATION_MODULE_LOAD_FAILED;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_INVALID_PARAM_NAME;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_LOADING_INTEGRATION_MODULE_CLASS;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_MISSING_MANDATORY_PARAMS;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_NO_INTEGRATION_MODULE;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_NO_INTEGRATION_PARAMS;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.ERR_ORG_NOT_EXISTS;
import static com.nasnav.integration.enums.IntegrationParam.DISABLED;
import static com.nasnav.integration.enums.IntegrationParam.INTEGRATION_MODULE;
import static com.nasnav.integration.enums.IntegrationParam.MAX_REQUEST_RATE;
import static java.lang.String.format;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import javax.annotation.PostConstruct;

import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.dao.IntegrationEventFailureRepository;
import com.nasnav.dao.IntegrationParamRepository;
import com.nasnav.dao.IntegrationParamTypeRepostory;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dto.IntegrationParamDTO;
import com.nasnav.dto.IntegrationParamDeleteDTO;
import com.nasnav.dto.OrganizationIntegrationInfoDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.enums.IntegrationParam;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;
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
import reactor.core.publisher.GroupedFlux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;

@Service
public class IntegrationServiceImpl implements IntegrationService {
	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	IntegrationParamRepository paramRepo;
	
	@Autowired
	IntegrationParamTypeRepostory paramTypeRepo;
	
	@Autowired
	OrganizationRepository orgRepo;
	
	
	private Map<Long, OrganizationIntegrationInfo> orgIntegration;
	
	private Set<IntegrationParamTypeEntity> mandatoryIntegrationParams;
	
	private FluxSink<EventHandling> eventFluxSink;
	private Flux<EventHandling> eventFlux;
	
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
		eventFlux =	emitterProcessor									
						.publishOn(scheduler) 																			
						.publish()
						.autoConnect();								
							
		eventFlux.groupBy(e -> e.getEvent().getOrganizationId())
				.subscribe(this::initOrganizationEventFlux);
		
		eventFluxSink = emitterProcessor.sink();		
	}
	
	
	
	private void initOrganizationEventFlux(GroupedFlux<Long, EventHandling> orgFlux) {
		Long orgId = orgFlux.key(); 
		Long eventDelay = Optional.ofNullable( orgIntegration.get(orgId) )
									.map(OrganizationIntegrationInfo::getRequestMinDelayMillis)
									.orElse(0L);
		
		orgFlux.delayElements(Duration.ofMillis(eventDelay) )
				.map(Mono::just)
				.subscribe(m -> m.subscribeOn( Schedulers.boundedElastic() )
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
				loadOrganizationIntegrationModule(orgId);
			}
			catch(Exception e) {
				String msg = String.format(ERR_INTEGRATION_MODULE_LOAD_FAILED, orgId);
				logger.error(msg, e);
				throwIntegrationInitException(msg);
			}
		}
	}






	private void loadOrganizationIntegrationModule(Long orgId) throws BusinessException {
		OrganizationIntegrationInfo integration = getOrganizationIntegrationInfo(orgId);
		this.orgIntegration.put(orgId, integration);
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
		Long minRequestDelay = getOrgMinRequestDelay(params);
		
		OrganizationIntegrationInfo info = new OrganizationIntegrationInfo(integrationModule, minRequestDelay, params);
		info.setDisabled( isModuleDisabled(params));
		
		return info;
	}






	private boolean isModuleDisabled(List<IntegrationParamEntity> params) {
		return Optional.ofNullable(params)
					.orElseGet(ArrayList::new)
					.stream()
					.map(IntegrationParamEntity::getType)
					.map(IntegrationParamTypeEntity::getTypeName)
					.anyMatch( name -> Objects.equals(name, DISABLED.getValue()) );
	}




	private Long getOrgMinRequestDelay(List<IntegrationParamEntity> params) {
		
		return params.stream()
					.filter( param -> Objects.equals( param.getType().getTypeName() , IntegrationParam.MAX_REQUEST_RATE.getValue()))
					.map( IntegrationParamEntity::getParamValue )
					.map( failSafeFunction(Long::valueOf) )
					.filter( Objects::nonNull )
					.map( this::calcMinRequestDelayMillis )
					.findFirst()
					.orElse(0L);
	}




	private Long calcMinRequestDelayMillis(Long requestRatePerSec) {
		if(requestRatePerSec == null || requestRatePerSec == 0) 
			return 0L;
		else
			return (long) ((1.0/requestRatePerSec)*1000);
	}
	
	


	private IntegrationModule loadIntegrationModule(Long orgId, List<IntegrationParamEntity> params)
			throws BusinessException {
		String integrationModuleClass = getIntegrationModuleClassName(orgId,params);
		
		return loadIntegrationModuleClass(orgId, integrationModuleClass);
	}






	private IntegrationModule loadIntegrationModuleClass(Long orgId, String integrationModuleClass
			) throws BusinessException {
		IntegrationModule integrationModule = null;
		
		try {
			@SuppressWarnings("unchecked")
			Class<IntegrationModule> moduleClass = (Class<IntegrationModule>) this.getClass().getClassLoader().loadClass(integrationModuleClass);
			integrationModule = moduleClass.getDeclaredConstructor(IntegrationService.class).newInstance(this);						
		} catch (Throwable e) {
			logger.error(e,e);
			throwIntegrationInitException(ERR_LOADING_INTEGRATION_MODULE_CLASS, integrationModuleClass, orgId);
		}
		
		return integrationModule;
	}




	
	
	private String getIntegrationModuleClassName(Long orgId, List<IntegrationParamEntity> params) throws BusinessException {
		String integrationModuleClass = params.stream()
												.filter(this::isIntegrationModuleNameParam)
												.map(IntegrationParamEntity::getParamValue)
												.findFirst()
												.orElseThrow(() -> getIntegrationInitException(ERR_NO_INTEGRATION_MODULE, orgId));
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
	public <E extends Event<T,R>, T, R> Mono<EventResult<T, R>> pushIntegrationEvent(E event, BiConsumer<E, Throwable> onError) throws InvalidIntegrationEventException {
		validateEvent(event);
		try {			
			eventFluxSink.next( EventHandling.of(event, onError) );	
			return event.getEventResult();
		}catch(Throwable e) {
			logger.error(e);
			runErrorCallback(event, e, onError);
			return event.getEventResult();
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
		if(integration == null || integration.isDisabled()) {			
			return; 	//ignore events if its organization has no integration info. loaded or is disabled.
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






	@Override
	public void registerIntegrationModule(OrganizationIntegrationInfoDTO info) throws BusinessException {
		validateIntegrationInfo(info);
		
		Long orgId = info.getOrganizationId();
		
		saveIntegrationParam(orgId, INTEGRATION_MODULE.getValue(), info.getIntegrationModule());
		saveIntegrationParam(orgId, MAX_REQUEST_RATE.getValue(), String.valueOf(info.getMaxRequestRate()) );
		
		info.getIntegrationParameters()
			.forEach((name, val) -> saveIntegrationParam(orgId, name, val));
		
		loadOrganizationIntegrationModule(orgId);
	}






	private void saveIntegrationParam(Long orgId, String typeName, String paramValue) {
		IntegrationParamTypeEntity type = getIntegrationParamType(typeName);
		
		IntegrationParamEntity param = paramRepo.findByOrganizationIdAndType_typeName(orgId, typeName)
												.orElseGet(IntegrationParamEntity::new);		
		if(param.getType() == null) {
			param.setType(type);
		}
		
		param.setOrganizationId(orgId);
		param.setParamValue(paramValue);
		
		paramRepo.save(param);
	}






	private IntegrationParamTypeEntity getIntegrationParamType(String typeName) {
		IntegrationParamTypeEntity type = paramTypeRepo.findByTypeName(typeName)
														.orElseGet(IntegrationParamTypeEntity::new);		
		if(type.getId() == null) {
			type = createIntegrationParamType(typeName, false);
		}
		return type;
	}






	private IntegrationParamTypeEntity createIntegrationParamType(String typeName, boolean isMandatory) {
		IntegrationParamTypeEntity type = new IntegrationParamTypeEntity();
		type.setTypeName(typeName.toUpperCase());
		type.setMandatory(isMandatory);
		return paramTypeRepo.save(type);
	}






	private void validateIntegrationInfo(OrganizationIntegrationInfoDTO info) throws BusinessException {
		Long orgId  = info.getOrganizationId();
		
		verifyOrgIntegrationFields(info, orgId);		
		verifyModuleClassExistsInClasspath(info, orgId);
	}






	private void verifyOrgIntegrationFields(OrganizationIntegrationInfoDTO info, Long orgId) throws BusinessException {
		if(info.getIntegrationModule() == null) {
			throw new BusinessException("No integration Module provided!"
									, "INVALID_PARAM:integration_module"
									, HttpStatus.NOT_ACCEPTABLE);
		}else if(orgId == null ) {
			throw new BusinessException("No organization id provided!"
									, "INVALID_PARAM:organization_id"
									, HttpStatus.NOT_ACCEPTABLE);
		}else if(!orgRepo.existsById(orgId)) {
			throw new BusinessException(format("No organization exists with id[%d]!", orgId)
									, "INVALID_PARAM:organization_id"
									, HttpStatus.NOT_ACCEPTABLE);
		}else if(info.getMaxRequestRate() < 1) {
			throw new BusinessException("Invalid max request per second provided!"
									, "INVALID_PARAM:max_request_rate"
									, HttpStatus.NOT_ACCEPTABLE);
		}
	}






	private void verifyModuleClassExistsInClasspath(OrganizationIntegrationInfoDTO info, Long orgId) throws BusinessException {
		try {
			loadIntegrationModuleClass(orgId, info.getIntegrationModule());
		}catch(Exception e) {
			throw new BusinessException("Invalid Integration Module provided!"
									, "INVALID_PARAM:integration_module"
									, HttpStatus.NOT_ACCEPTABLE);
		}		
	}






	@Override
	public void disableIntegrationModule(Long organizationId) throws BusinessException {
		if(organizationId == null 
				|| !orgRepo.existsById(organizationId)
				|| !orgIntegration.containsKey(organizationId) ) {
			return;
		}
		saveIntegrationParam(organizationId, DISABLED.getValue(), "TRUE");
		loadOrganizationIntegrationModule(organizationId);
	}






	@Override
	@Transactional
	public void enableIntegrationModule(Long organizationId) throws BusinessException {
		if(organizationId == null || !orgIntegration.containsKey(organizationId) ) {
			throw new BusinessException(
					format(ERR_NO_INTEGRATION_MODULE, organizationId) 
					, "INVALID_PARAM:organization_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		
		deleteIntegrationParam(organizationId, DISABLED.getValue());
		loadOrganizationIntegrationModule(organizationId);
	}






	private void deleteIntegrationParam(Long orgId, String paramTypeName) {
		paramRepo.deleteByOrganizationIdAndType_typeName(orgId, paramTypeName);
	}






	@Override
	public void clearAllIntegrationModules() {
		orgIntegration.clear();
	}






	@Override
	@Transactional
	public void removeIntegrationModule(Long organizationId) {
		paramRepo.deleteByOrganizationId(organizationId);		
		orgIntegration.remove(organizationId);
	}






	@Override
	public void addIntegrationParam(IntegrationParamDTO param) throws BusinessException {
		validateIntegrationParam(param);
		saveIntegrationParam(param.getOrganizationId(), param.getParamName(), param.getParamValue());
	}






	private void validateIntegrationParam(IntegrationParamDTO param) throws BusinessException {
		validateOrganizationExists(param.getOrganizationId());		
		validateIntegrationParamName( param.getParamName() );
	}






	private void validateIntegrationParamName(String name) throws BusinessException {
		if(name == null || !name.matches("^[A-Z0-9_]+")) {
			throw new BusinessException(
					format(ERR_INVALID_PARAM_NAME, name)
					, "INVALID_PARAM: param_name"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}






	private void validateOrganizationExists(Long orgId) throws BusinessException {
		if(orgId == null || !orgRepo.existsById(orgId)) {
			throw new BusinessException(
					format(ERR_ORG_NOT_EXISTS, orgId)
					, "INVALID_PARAM: organization_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
	}






	@Override
	@Transactional
	public void deleteIntegrationParam(IntegrationParamDeleteDTO param) throws BusinessException {
		if(param.getOrganizationId() == null || param.getParamName() == null) {
			return;
		}
		
		deleteIntegrationParam(param.getOrganizationId(), param.getParamName());		
	}






	@Override
	public List<OrganizationIntegrationInfoDTO> getAllIntegrationModules() {
		return orgIntegration.entrySet()
							.stream()
							.map(this::toOrganizationIntegrationInfoDTO)
							.collect(Collectors.toList());					
	}
	
	
	
	
	
	private OrganizationIntegrationInfoDTO toOrganizationIntegrationInfoDTO(Map.Entry<Long, OrganizationIntegrationInfo> infoEntry) {
		OrganizationIntegrationInfoDTO dto = new OrganizationIntegrationInfoDTO();
		Long orgId = infoEntry.getKey();
		OrganizationIntegrationInfo info = infoEntry.getValue();
		Map<String,String> params = info.getParameters()
										.stream()
										.collect(Collectors.toMap(IntegrationParamEntity::getParameterTypeName
																, IntegrationParamEntity::getParamValue));		
		dto.setOrganizationId(orgId);
		dto.setIntegrationModule(info.getIntegrationModule().getClass().getName());
		dto.setMaxRequestRate(1000/info.getRequestMinDelayMillis().intValue());
		dto.setIntegrationParameters(params);
		
		return dto;
	}

}



@Data
@AllArgsConstructor
class EventHandling<E extends Event<T,R>, T, R>{
	private E event;
	private BiConsumer<E, Throwable> onError;
	
	
	public static <E extends Event<T,R>, T, R> EventHandling<E,T,R> of(E event, BiConsumer<E, Throwable> onError){
		return new EventHandling<E,T,R>(event, onError);
	} 
}
