package com.nasnav.integration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dao.*;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.exceptions.ImportProductRuntimeException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.integration.events.*;
import com.nasnav.integration.events.data.*;
import com.nasnav.integration.exceptions.InvalidIntegrationEventException;
import com.nasnav.integration.model.ImportedShop;
import com.nasnav.integration.model.OrganizationIntegrationInfo;
import com.nasnav.persistence.*;
import com.nasnav.request.GetIntegrationDictParam;
import com.nasnav.request.GetIntegrationErrorParam;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.DataImportService;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ShopService;
import com.nasnav.service.model.ImportedImage;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.jboss.logging.Logger;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;

import javax.annotation.PostConstruct;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.time.Duration;
import java.util.*;
import java.util.function.BiConsumer;

import static com.nasnav.commons.utils.StringUtils.*;
import static com.nasnav.constatnts.error.integration.IntegrationServiceErrors.*;
import static com.nasnav.enumerations.Roles.NASNAV_ADMIN;
import static com.nasnav.enumerations.Roles.ORGANIZATION_ADMIN;
import static com.nasnav.integration.enums.IntegrationParam.*;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static com.nasnav.integration.enums.MappingType.SHOP;
import static java.lang.String.format;
import static java.time.Duration.ofMillis;
import static java.util.Collections.*;
import static java.util.Objects.isNull;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static org.springframework.beans.factory.config.ConfigurableBeanFactory.SCOPE_SINGLETON;
import static org.springframework.http.HttpStatus.*;
import static reactor.core.scheduler.Schedulers.boundedElastic;

@Service
@Scope(value = SCOPE_SINGLETON)
public class IntegrationServiceImpl implements IntegrationService {
	private static final long PRODUCT_IMPORT_REQUEST_TIMEOUT_MIN = 5760L;

	private static final Integer MAX_PG_SIZE = 500;

	public static long STOCK_REQUEST_TIMEOUT = 30;

	public static  long REQUEST_TIMEOUT_SEC = 300L;

	private final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	private IntegrationParamRepository paramRepo;
	
	@Autowired
	private IntegrationParamTypeRepostory paramTypeRepo;
	
	@Autowired
	private OrganizationRepository orgRepo;
	
	@Autowired
	private IntegrationMappingRepository mappingRepo;
	
	@Autowired
	private IntegrationMappingTypeRepository mappingTypeRepo;
	
	@Autowired
	private ShopService shopService;
	
	@Autowired
	private SecurityService securityService;
	
	@Autowired
	private DataImportService dataImportService;
	
	@Autowired
	private ProductVariantsRepository variantRepo;
	
	@Autowired
	private ShopsRepository shopRepo;

	
	@Autowired
	private ProductImageService imgService;
	
	@Autowired
	private IntegrationUtils utils;
	
	@Autowired
	private IntegrationServiceHelper integrationHelper;
	
	@SuppressWarnings("rawtypes")
	private FluxSink<EventHandling> eventFluxSink;
	
	@SuppressWarnings("rawtypes")
	private Flux<EventHandling> eventFlux;
	
	@Autowired
	private IntegrationEventFailureRepository eventFailureRepo;
	
	@Autowired
	private IntegrationModulesService modules;
	
	
	
	@PostConstruct
	public void init() throws BusinessException {
		loadIntegrationModules();
		initEventFlux();
	}


	
	private void initEventFlux() {
		Scheduler scheduler = boundedElastic();
		@SuppressWarnings("rawtypes")
		EmitterProcessor<EventHandling> emitterProcessor = EmitterProcessor.create();
		eventFlux =	emitterProcessor									
						.publishOn(scheduler) 																			
						.publish()
						.autoConnect();								
		eventFlux.groupBy(e -> e.getEvent().getOrganizationId())
				.subscribe(this::initOrganizationEventFlux);
		eventFluxSink = emitterProcessor.sink();
	}
	
	
	
	@SuppressWarnings("rawtypes")
	private void initOrganizationEventFlux(GroupedFlux<Long, EventHandling> orgFlux) {
		Long orgId = orgFlux.key(); 
		Long eventDelay = ofNullable(modules.getIntegrationInfo(orgId))
							.map(OrganizationIntegrationInfo::getRequestMinDelayMillis)
							.orElse(0L);
		
		orgFlux.delayElements(ofMillis(eventDelay) )
				.map(Mono::just)
				.subscribe(m -> m.subscribeOn( boundedElastic() )
				.subscribe(this::handle));
	}


	
	







	@Override
	public void setIntegrationModule(Long orgId, String classFullName) {
		// TODO Auto-generated method stub

	}



	@Override
	public IntegrationModule getIntegrationModule(Long orgId) {
		return modules.getIntegrationModule(orgId);
	}


	@Override
	public void loadIntegrationModules() throws BusinessException {	
		modules.loadIntegrationModules(this);
	}

	
	
	private RuntimeBusinessException getIntegrationRuntimeException(String msg, Object... args) {
		return new RuntimeBusinessException( String.format( msg, args )
										, "INTEGRATION FAILURE"
										, HttpStatus.INTERNAL_SERVER_ERROR);
	}



	@Override
	@Transactional
	public void addMappedValue(Long orgId, MappingType type, String localValue, String remoteValue) throws BusinessException {
		String typeName = type.getValue();				
		
		deleteExistingMappingOfLocalAndRemoteValues(orgId, typeName, localValue, remoteValue);		
		saveIntegrationMapping(orgId, typeName, localValue, remoteValue);		
	}



	private void saveIntegrationMapping(Long orgId, String typeName, String localValue, String remoteValue)
			throws BusinessException {
		IntegrationMappingTypeEntity mappingType = mappingTypeRepo.findByTypeName(typeName)
																.orElseThrow(() -> getNonExistingMappingTypeException(typeName) );
		IntegrationMappingEntity mapping = new IntegrationMappingEntity(orgId, mappingType, localValue, remoteValue);
		mappingRepo.save(mapping);
	}



	private void deleteExistingMappingOfLocalAndRemoteValues(Long orgId, String typeName, String localValue, String remoteValue) {
		IntegrationMappingTypeEntity type = mappingTypeRepo.findByTypeName(typeName).orElse(null);
		mappingRepo.deleteByLocalValue(orgId, type, localValue);
		mappingRepo.deleteByRemoteValue(orgId, type, remoteValue);
	}



	@Override
	public String getRemoteMappedValue(Long orgId, MappingType type, String localValue) {
		String val= null;
		try {
			val = mappingRepo
					.findByOrganizationIdAndMappingType_typeNameAndLocalValue(orgId, type.getValue(), localValue)
					.map(IntegrationMappingEntity::getRemoteValue)
					.orElse(null);
		}catch(Throwable e) {
			logger.error(e,e);
		}
		
		return val;
	}
	
	
	
	
	

	@Override
	public String getLocalMappedValue(Long orgId, MappingType type, String remoteValue) {
		return mappingRepo.findByOrganizationIdAndMappingType_typeNameAndRemoteValue(orgId, type.getValue(), remoteValue)
							.map(IntegrationMappingEntity::getLocalValue)
							.orElse(null);
	}



	@Override
	@Transactional(rollbackFor = Throwable.class)
	public List<Long> importShops() throws Throwable {	
		
		validateImportShopRequest();
		
		Long orgId = securityService.getCurrentUserOrganizationId();
		ShopsImportEvent importShopEvent =
				Event.ofType(ShopsImportEvent.class)
					.setEventInfo(orgId, new ShopsFetchParam())
					.build();

		return pushIntegrationEvent(importShopEvent, (e,t) -> {throw new RuntimeException("Failed To import shops of org "+ orgId);})
				.block(Duration.ofSeconds(REQUEST_TIMEOUT_SEC))
				.getReturnedData()
				.stream()
				.filter(Objects::nonNull)
				.filter(extShop -> isExternalShopNotExists(orgId, extShop))
				.map(extShop -> importExternalShop(orgId, extShop))
				.collect(toList());
	}
	
	
	
	
	
	private void validateImportShopRequest() throws BusinessException {
		Long orgId = securityService.getCurrentUserOrganizationId();
		if(!modules.hasIntegrationModule(orgId)) {
			throw utils.getIntegrationInitException(ERR_NO_INTEGRATION_MODULE, orgId);
		}
	}






	private Boolean isExternalShopNotExists(Long orgId, ImportedShop externalShop) {
		return !isExternalShopExists(orgId, externalShop);
	}
	
	
	
	
	
	private Boolean isExternalShopExists(Long orgId, ImportedShop externalShop) {
		return getLocalMappedValue(orgId , SHOP, externalShop.getId()) != null;
	}
	
	
	
	
	
	
	private Long importExternalShop(Long orgId, ImportedShop externalShop) {
		String shopName = externalShop.getName();
		
		Long shopId = null;
		try {
			shopId = shopRepo
						.findByNameAndOrganizationEntity_IdAndRemoved(shopName, orgId, 0)
						.map(ShopsEntity::getId)
						.orElseGet(() -> createNewShop(externalShop, orgId));

			addMappedValue(orgId, SHOP, String.valueOf(shopId), String.valueOf(externalShop.getId()));
		} catch (Throwable e) {
			logger.error(e,e);
			throw new RuntimeException(
						format(ERR_SHOP_IMPORT_FAILED, externalShop.getId()));
		}		
		return shopId;
	}
	

	
	
	

	private Long createNewShop(ImportedShop externalShop, Long orgId)  {
		ShopJsonDTO shopUpdateDto = new ShopJsonDTO();
		shopUpdateDto.setName(externalShop.getName());
		
		ShopResponse response;
		try {
			response = shopService.shopModification(shopUpdateDto);
		} catch (Throwable e) {
			logger.error(e,e);
			throw new RuntimeException(
						format(ERR_SHOP_IMPORT_FAILED, externalShop.getId()));
		}
		return response.getStoreId();
	}






	@Override
	public void mapToIntegratedShop(Long shopId, ImportedShop integratedShop) {
		// TODO Auto-generated method stub

	}
	
	
	
	
	

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public Integer importOrganizationProducts(IntegrationProductImportDTO metadata) throws Throwable {
		
		importShops();
		
		Long orgId = securityService.getCurrentUserOrganizationId();
		
		ProductImportMetadata commonImportMetaData = getImportMetaData(metadata);
		
		IntegrationImportedProducts importedProducts = getProductsFromExternalSystem(orgId, metadata);
		
		importProductsIntoNasnav(commonImportMetaData, importedProducts);
		
		return importedProducts.getTotalPages();
	}






	
	


	private void importProductsIntoNasnav(ProductImportMetadata commonImportMetaData,
			IntegrationImportedProducts importedProducts) {
		importedProducts
			.getAllShopsProducts()
			.stream()
			.map(shopProducts -> toShopProductsImportData(shopProducts, commonImportMetaData))
			.forEach(this::importSingleShopProducts);
	}






	private IntegrationImportedProducts getProductsFromExternalSystem(Long orgId, IntegrationProductImportDTO metadata)
			throws Throwable, InvalidIntegrationEventException {
		Integer pageCount = recitfyPageCount(metadata.getPageCount());
		Integer pageNum = recitfyPageNum(metadata.getPageNum());
		
		ProductImportEventParam importParam = new ProductImportEventParam(pageNum, pageCount);
		ProductsImportEvent event =
				Event.ofType(ProductsImportEvent.class)
					.setEventInfo(orgId, importParam)
					.build();

		IntegrationImportedProducts importedProducts = 
				pushIntegrationEvent(event, this::onProductImportError)
					.blockOptional(Duration.ofMinutes(PRODUCT_IMPORT_REQUEST_TIMEOUT_MIN))
					.orElseThrow(() -> getNoDataReturnedException(orgId))
					.getReturnedData();
		
		if(importedProducts.getAllShopsProducts() == null 
				||importedProducts.getAllShopsProducts( ).isEmpty()) {
			throw  getNoDataReturnedException(orgId);
		}
		
		addEveryShopLocalId(importedProducts);
		
		return filterProductsWithNoEixstingShop(importedProducts);
	}

	
	
	
	private IntegrationImportedProducts filterProductsWithNoEixstingShop(IntegrationImportedProducts importedProducts) {
		List<ShopImportedProducts> filteredAllShopsProducts = 
				importedProducts
					.getAllShopsProducts()
					.stream()
					.filter(shopProducts -> shopProducts.getShopId() > 0)
					.collect(toList());
		return new IntegrationImportedProducts(importedProducts.getTotalPages(), filteredAllShopsProducts);
	}






	private Integer recitfyPageCount(Integer value) {
		return ofNullable(value)
				.map(val -> val <= 0? 1: val)
				.orElse(1000);
	}

	
	
	
	private Integer recitfyPageNum(Integer value) {
		return ofNullable(value)
				.map(val -> val <= 0? 1: val)
				.orElse(1);
	}

	
	
	
	private void addEveryShopLocalId(IntegrationImportedProducts importedProducts) {
		importedProducts
			.getAllShopsProducts()
			.stream()
			.forEach(this::addShopLocalId);
	}


	
	
	
	
	private void addShopLocalId(ShopImportedProducts shopProducts) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		String externalShopId = shopProducts.getExternalShopId();
		String shopIdStr = ofNullable(getLocalMappedValue(orgId, SHOP, externalShopId)).orElse("-1");
				
		Long shopId = -1L;
		
		try {
			shopId = Long.valueOf(shopIdStr.toString());
		}catch(Throwable t) {
			logger.error(t,t);
			throw new RuntimeBusinessException(
					format(ERR_EXTERNAL_SHOP_NOT_FOUND, externalShopId)
					, "INTEGRATION FAILURE"
					, INTERNAL_SERVER_ERROR);
		}
		
		shopProducts.setShopId(shopId);
	}

	
	
	
	


	private Throwable getNoDataReturnedException(Long orgId) {
		return getIntegrationRuntimeException(ERR_NO_PRODUCT_DATA_RETURNED, orgId);
	}


	


	private ProductImportMetadata getImportMetaData(IntegrationProductImportDTO metadata) {
		ProductImportMetadata commonImportMetaData = new ProductImportMetadata();
		commonImportMetaData.setCurrency(metadata.getCurrency());
		commonImportMetaData.setDryrun(metadata.isDryrun());
		commonImportMetaData.setEncoding(metadata.getEncoding());
		commonImportMetaData.setUpdateProduct(metadata.isUpdateProduct());
		commonImportMetaData.setUpdateStocks(metadata.isUpdateStocks());
		commonImportMetaData.setResetTags(metadata.isResetTags());
		commonImportMetaData.setDeleteOldProducts(metadata.isDeleteOldProducts());
		commonImportMetaData.setInsertNewProducts(metadata.isInsertNewProducts());
		return commonImportMetaData;
	}
	
	
	
	
	
	private void importSingleShopProducts(ProductImportInputData inputData) {
		try {
			logger.info(format(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> importing products of shop[%d]"
					, inputData.getMetadata().getShopId()));
			ImportProductContext context = dataImportService.importProducts(inputData.getProducts(), inputData.getMetadata());
			ObjectMapper mapper = new ObjectMapper();
			String importReport = mapper.writeValueAsString(context);
			logger.info(importReport);
		} catch (BusinessException e) {
			logger.error(e,e);
			throw new RuntimeBusinessException(e);
		} catch (JsonProcessingException e) {
			logger.error(e,e);
			throw new RuntimeException(e);
		} catch (ImportProductException e) {
			throw new ImportProductRuntimeException(e, e.getContext());
		}
	}
	
	
	
	
	
	private ProductImportInputData toShopProductsImportData(ShopImportedProducts shopProducts, ProductImportMetadata commonMetaData) {
				
		ProductImportMetadata metadata = new ProductImportMetadata();
		BeanUtils.copyProperties(commonMetaData, metadata);
		metadata.setShopId(shopProducts.getShopId());
		
		ProductImportInputData importData = new ProductImportInputData();
		importData.setProducts(shopProducts.getImportedProducts());
		importData.setMetadata(metadata);
		
		return importData;
	}
	
	
	
	
	
	private void onProductImportError(ProductsImportEvent event, Throwable t) {
		logger.error(t,t);
		throw getIntegrationRuntimeException("Failed to import products from external system of organization [%d]!", event.getOrganizationId());
	}
	
	
	
	private void onImagesImportError(ImagesImportEvent event, Throwable t) {
		logger.error(t,t);
		throw getIntegrationRuntimeException("Failed to import products from external system of organization [%d]!", event.getOrganizationId());
	}
	
	
	
	

	@Override
	public Optional<Integer> getExternalStock(Long localVariantId, Long localShopId) throws BusinessException {
		
		validateStockFetchParam(localVariantId, localShopId);		
		
		Long orgId = securityService.getCurrentUserOrganizationId();
		String externalVariantId = getRemoteMappedValue(orgId, PRODUCT_VARIANT, nullableToString(localVariantId));
		String externalShopId = getRemoteMappedValue(orgId, SHOP, nullableToString(localShopId));
		
		//TODO: the part that returns the local stock as a fallback, it should be moved to the order service, as it is a part of 
		//the order logic , not the integration.
		//this method should instead throw a well-know exception if no stock were found, and upper layers decides what to user instead
		//-------------------------------------------------------------
		if(anyBlankOrNull(externalVariantId, externalShopId)) {
			throw new BusinessException(
					format("Invalid stock parameters : variantId-shopId are [%d-%d]", localVariantId, localShopId)
					, "INVALID PARAMETERS"  
					, NOT_ACCEPTABLE);
		}
		//-------------------------------------------------------------
		
		StockFetchEvent event = createStockFetchEvent(orgId, externalVariantId, externalShopId);
		
		//the webclient will return empty Mono if the response was not OK
		return pushIntegrationEvent(event, (e,t) -> handleStockFetchFailure(localVariantId, localShopId, t))
				.blockOptional(Duration.ofSeconds(STOCK_REQUEST_TIMEOUT))
				.map(res -> res.getReturnedData());
	}






	private StockFetchEvent createStockFetchEvent(Long orgId, String externalVariantId, String externalShopId) {					
		StockEventParam param = new StockEventParam(externalVariantId, externalShopId);
		return Event.ofType(StockFetchEvent.class)
				.setEventInfo(orgId, param)
				.build();
	}
	
	
	
	
	
	
	private void validateStockFetchParam(Long variantId, Long shopId) throws BusinessException {
		if(variantId == null || shopId == null) {
			throw new BusinessException(
					format(ERR_FETCH_STOCK_NULL_PARAMETERS, variantId, shopId)
					, "INVALID INTEGRATION OPERATION"
					, HttpStatus.INTERNAL_SERVER_ERROR);			
		}
		
		boolean variantExists = variantRepo.existsById(variantId);
		boolean shopdExists = shopRepo.existsById(shopId);
		
		if(!variantExists) {
			throw new BusinessException(
						format(ERR_FETCH_STOCK_VARIANT_NOT_EXISTS, variantId)
						, "INVALID INTEGRATION OPERATION"
						, INTERNAL_SERVER_ERROR);
		}
		
		if(!shopdExists) {
			throw new BusinessException(
						format(ERR_FETCH_STOCK_SHOP_NOT_EXISTS, variantId, shopId)
						, "INVALID INTEGRATION OPERATION"
						, INTERNAL_SERVER_ERROR);
		}
	}




	
	
	
	
	private void handleStockFetchFailure(Long variantId, Long shopId, Throwable t) {
		logger.error(t,t);
		throw new RuntimeBusinessException(
				format("Failed To get stocks of variant [%d] for shop [%d]", variantId, shopId)
				, "EXTERNAL STOCK FETCH FAILED"
				, INTERNAL_SERVER_ERROR);
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
	
	
	
	
	
	private BusinessException getNonExistingMappingTypeException(String typeName) {
		return new BusinessException(format(
				ERR_MAPPING_TYPE_NOT_EXISTS, typeName)
				, "INTEGRATION MAPPING FAILED"
				, HttpStatus.INTERNAL_SERVER_ERROR);
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
		Long orgId = event.getOrganizationId();
		OrganizationIntegrationInfo integration =  modules.getIntegrationInfo(orgId);
		if(integration == null || integration.isDisabled()) {			
			//ignore events if its organization has no integration info. loaded or is disabled.
			return; 	
		}
		
		logTraceEventHandling(event);
		
		integration.getIntegrationModule()
					.pushEvent(handling);		
	}






	private <E extends Event<T, R>, T,R> void logTraceEventHandling(E event) {
		logger.tracef("Handling Event of type [%s] for org[%d] with data[%s]"
					, event.getClass().getName()
					, event.getOrganizationId()
					, event.getEventInfo().getEventData());
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
		
		try {
			modules.loadOrganizationIntegrationModule(orgId, this);
		}catch(BusinessException e) {
			logger.error(e,e);
			throw new BusinessException( 
					format(ERR_INTEGRATION_MODULE_LOAD_FAILED, orgId)
					,"INVALID INTEGRATION INFO"
					, HttpStatus.NOT_ACCEPTABLE);
		}
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
		String integrationModuleClass = info.getIntegrationModule();
		try {
			this.getClass().getClassLoader().loadClass(integrationModuleClass);
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
				|| !modules.hasIntegrationModule(organizationId) ) {
			return;
		}
		saveIntegrationParam(organizationId, DISABLED.getValue(), "TRUE");
		modules.loadOrganizationIntegrationModule(organizationId, this);
	}



	@Override
	@Transactional
	public void enableIntegrationModule(Long organizationId) throws BusinessException {
		if(organizationId == null || !modules.hasIntegrationModule(organizationId) ) {
			throw new BusinessException(
					format(ERR_NO_INTEGRATION_MODULE, organizationId) 
					, "INVALID_PARAM:organization_id"
					, HttpStatus.NOT_ACCEPTABLE);
		}
		deleteIntegrationParam(organizationId, DISABLED.getValue());
		modules.loadOrganizationIntegrationModule(organizationId, this);
	}



	private void deleteIntegrationParam(Long orgId, String paramTypeName) {
		paramRepo.deleteByOrganizationIdAndType_typeName(orgId, paramTypeName);
	}



	@Override
	public void clearAllIntegrationModules() {
		modules.clearAllIntegrationModules();
	}



	@Override
	@Transactional
	public void removeIntegrationModule(Long organizationId) {
		modules.removeIntegrationModule(organizationId);
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
		return modules.getAllIntegrationModules();
	}



	@Override
	@Transactional
	public void deleteMappingByLocalValue(Long orgId, MappingType type, String mappingLocalVal) {
		IntegrationMappingTypeEntity typeEntity = mappingTypeRepo.findByTypeName(type.getValue()).orElse(null);
		mappingRepo.deleteByLocalValue(orgId, typeEntity, mappingLocalVal);
	}



	@Override
	@Transactional
	public void deleteMappingByRemoteValue(Long orgId, MappingType type, String mappingRemoteVal) {
		IntegrationMappingTypeEntity typeEntity = mappingTypeRepo.findByTypeName(type.getValue()).orElse(null);
		mappingRepo.deleteByRemoteValue(orgId, typeEntity, mappingRemoteVal);
	}



	@Override
	public String getIntegrationParamValue(Long orgId, String paramName) {
		return getParamFromDB(orgId, paramName);
	}



	private String getParamFromDB(Long orgId, String paramName) {
		return paramRepo.findByOrganizationIdAndType_typeName(orgId, paramName)
								.map(IntegrationParamEntity::getParamValue)
							    .orElse(null);
	}




	@Override
	public <E extends Event<T, R>, T, R> void retryEvent(E event, BiConsumer<E, Throwable> onError, Duration delay, Integer maxRetryCount) {
		event.incrementRetryCount();
		if(event.getRetryCount() >= maxRetryCount) {
			return;
		}
		
		Mono.just(event)
			.publishOn(boundedElastic())
			.delayElement(delay)			
			.subscribe(ev -> wrappedPushIntegrationEvent(ev, onError));
	}
	
	
	
	
	
	public <E extends Event<T,R>, T, R> Mono<EventResult<T, R>> wrappedPushIntegrationEvent(E event, BiConsumer<E, Throwable> onError)  {
		try {
			return pushIntegrationEvent(event, onError);
		} catch (InvalidIntegrationEventException e) {
			throw new RuntimeBusinessException(e);
		}
	}






	@Override
	public ResponsePage<IntegrationDictionaryDTO> getIntegrationDictionary(GetIntegrationDictParam param) throws BusinessException {
		verifyGetIntegrationDictParam(param);

		GetIntegrationDictParam rectifiedParams = rectifyGetIntegrationDictParams(param);
		
		Pageable pageable = createDictPageSpecs(rectifiedParams);
		
		Page<IntegrationMappingEntity> dictPage = Page.empty();
		
		if(isNull(param.getDict_type())) {
			dictPage = findMappingByOrg(rectifiedParams, pageable);
		}else {
			dictPage = findMappingByOrgAndType(rectifiedParams, pageable);
		}
				
		return toPageOfIntegrationDictDTO(dictPage);
	}






	private GetIntegrationDictParam rectifyGetIntegrationDictParams(GetIntegrationDictParam param) {
		Long orgId = ofNullable(param.getOrg_id()).orElse(0L);
		if(securityService.currentUserHasRole(ORGANIZATION_ADMIN)) {
			orgId = securityService.getCurrentUserOrganizationId();
		}
				
		Integer pageSize = 
				ofNullable(param.getPage_size())
					.map(this::capResponsePageSize)
					.orElse(MAX_PG_SIZE);
		Integer pageNum = 
				ofNullable(param.getPage_num())
					.map(this::capErrorPageNum)
					.orElse(1);
		
		GetIntegrationDictParam rect = new GetIntegrationDictParam();
		rect.setOrg_id(orgId);
		rect.setPage_size(pageSize);
		rect.setPage_num(pageNum);
		rect.setDict_type(param.getDict_type());
		
		return rect;
	}
	
	
	
	private Pageable createDictPageSpecs(GetIntegrationDictParam rectifiedParams) {
		return
			PageRequest.of(
					rectifiedParams.getPage_num() - 1
					, rectifiedParams.getPage_size()
					, Sort.by("id").descending());
	}






	private void verifyGetIntegrationDictParam(GetIntegrationDictParam param) throws BusinessException {
		Long currentUserOrgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);
		Long orgId = param.getOrg_id();
		
		if(!isNasnavAdmin && !Objects.equals(currentUserOrgId, orgId)) {
			throw new BusinessException(
					format("Current User cannot view Dictionary of Organization[]", orgId)
					, "INVALID PARAM: org_id"
					, FORBIDDEN);			
		}		
	}






	@Override
	public ResponsePage<IntegrationErrorDTO> getIntegrationErrors(GetIntegrationErrorParam param) throws BusinessException {
		verifyGetIntegrationErrorsParam(param);
		
		GetIntegrationErrorParam rectifiedParams = rectifyGetIntegrationErrorsParams(param);
		
		Pageable pageable = createErrorPageSpecs(rectifiedParams);
		
		Page<IntegrationEventFailureEntity> failuresPage = Page.empty();
		
		if(isBlankOrNull(param.getEvent_type())) {
			failuresPage = findFailuresByOrg(rectifiedParams, pageable);
		}else {
			failuresPage = findFailuresByOrgAndType(rectifiedParams, pageable);
		}
				
		return toPageOfIntegrationErrorDTO(failuresPage);
	}






	private void verifyGetIntegrationErrorsParam(GetIntegrationErrorParam param) throws BusinessException {
		Long currentUserOrgId = securityService.getCurrentUserOrganizationId();
		boolean isNasnavAdmin = securityService.currentUserHasRole(NASNAV_ADMIN);
		Long orgId = param.getOrg_id();
		
		if(!isNasnavAdmin && !Objects.equals(currentUserOrgId, orgId)) {
			throw new BusinessException(
					format("Current User cannot view errors of Organization[]", orgId)
					, "INVALID PARAM: org_id"
					, FORBIDDEN);
			
		}
	}






	private Pageable createErrorPageSpecs(GetIntegrationErrorParam rectifiedParams) {
		return
			PageRequest.of(
					rectifiedParams.getPage_num() - 1
					, rectifiedParams.getPage_size()
					, Sort.by("createdAt").descending());
	}






	private Page<IntegrationEventFailureEntity> findFailuresByOrgAndType(GetIntegrationErrorParam rectifiedParams,
			Pageable pageable) {
		return
			eventFailureRepo
				.findByOrganizationIdAndEventType(
					rectifiedParams.getOrg_id()
					, rectifiedParams.getEvent_type()
					, pageable);
	}






	private Page<IntegrationEventFailureEntity> findFailuresByOrg(GetIntegrationErrorParam rectifiedParams,
			Pageable pageable) {
		return
			eventFailureRepo
				.findByOrganizationId(
					rectifiedParams.getOrg_id()
					, pageable);
	}
	
	
	
	
	
	private Page<IntegrationMappingEntity> findMappingByOrg(GetIntegrationDictParam rectifiedParams,
			Pageable pageable) {
		return
			mappingRepo
				.findByOrganizationId(
					rectifiedParams.getOrg_id()
					, pageable);
	}

	
	
	
	private Page<IntegrationMappingEntity> findMappingByOrgAndType(GetIntegrationDictParam rectifiedParams,
			Pageable pageable) {
		return
			mappingRepo
				.findByOrganizationIdAndMappingType_typeName(
					rectifiedParams.getOrg_id()
					, rectifiedParams.getDict_type().getValue()
					, pageable);
	}







	private GetIntegrationErrorParam rectifyGetIntegrationErrorsParams(GetIntegrationErrorParam param) {
		Long orgId = ofNullable(param.getOrg_id()).orElse(0L);
		if(securityService.currentUserHasRole(ORGANIZATION_ADMIN)) {
			orgId = securityService.getCurrentUserOrganizationId();
		}
				
		Integer pageSize = 
				ofNullable(param.getPage_size())
					.map(this::capResponsePageSize)
					.orElse(MAX_PG_SIZE);
		Integer pageNum = 
				ofNullable(param.getPage_num())
					.map(this::capErrorPageNum)
					.orElse(1);
		
		GetIntegrationErrorParam rect = new GetIntegrationErrorParam();
		rect.setOrg_id(orgId);
		rect.setPage_size(pageSize);
		rect.setPage_num(pageNum);
		
		return rect;
	}


	
	
	
	private Integer capResponsePageSize(Integer pageSize) { 
		return pageSize > MAX_PG_SIZE ? MAX_PG_SIZE: pageSize;
	}
	
	
	
	
	
	private Integer capErrorPageNum(Integer pageNum) {
		return pageNum >= 1 ? pageNum : 1;
	}




	private ResponsePage<IntegrationErrorDTO> toPageOfIntegrationErrorDTO(Page<IntegrationEventFailureEntity> failuresPage) {
		List<IntegrationErrorDTO> content = 
				ofNullable(failuresPage)
					.map(Page::getContent)
					.orElse(emptyList())
					.stream()
					.map(this::toIntegrationErrorDTO)
					.collect(toList());
		
		return createResponsePage(failuresPage, content);
	}
	
	
	
	
	private ResponsePage<IntegrationDictionaryDTO> toPageOfIntegrationDictDTO(Page<IntegrationMappingEntity> dictPage) {
		List<IntegrationDictionaryDTO> content = 
				ofNullable(dictPage)
					.map(Page::getContent)
					.orElse(emptyList())
					.stream()
					.map(this::toIntegrationDictionaryDTO)
					.collect(toList());
		
		return createResponsePage(dictPage, content);
	}

	
	
	
	private IntegrationDictionaryDTO toIntegrationDictionaryDTO(IntegrationMappingEntity entity) {
		IntegrationDictionaryDTO dto = new IntegrationDictionaryDTO();
		
		dto.setOrgId(entity.getOrganizationId());
		dto.setLocalValue(entity.getLocalValue());
		dto.setRemoteValue(entity.getRemoteValue());
		dto.setTypeName(entity.getMappingTypeName());
		
		return dto;
	}





	private <T,E> ResponsePage<T>  createResponsePage(Page<E> entityPage,
			List<T> content) {
		ResponsePage<T> response = new ResponsePage<>();
		
		response.setContent(content);
		response.setPageSize( entityPage.getPageable().getPageSize());
		response.setPageNumber( entityPage.getPageable().getPageNumber() + 1);
		response.setTotalElements( entityPage.getTotalElements());
		response.setTotalPages(entityPage.getTotalPages());
		
		return response;
	}
	
	
	
	
	
	private IntegrationErrorDTO toIntegrationErrorDTO(IntegrationEventFailureEntity entity) {
		IntegrationErrorDTO dto = new IntegrationErrorDTO();
		
		dto.setCreatedAt(entity.getCreatedAt());
		dto.setEventData(entity.getEventData());
		dto.setEventType(entity.getEventType());
		dto.setFallbackException(entity.getFallbackException());
		dto.setHandleException(entity.getHandleException());
		dto.setId(entity.getId());
		
		return dto;
	}






	@Override
	public Map<String,String> getLocalMappedValues(Long orgId, MappingType type, List<String> externalValues) {
		return integrationHelper.getLocalMappedValues(orgId, type, externalValues);
	}






	@Override
	public ResponsePage<Void> importProductImages(IntegrationImageImportDTO param) throws BusinessException {
		Integer pageCount = recitfyPageCount(param.getPageCount());
		Integer pageNum = recitfyPageNum(param.getPageNum());
		
		ImportedImagesPage imgsPage = importImagesFromExternalSystem(param, pageCount, pageNum);
		
		imgService.saveImgsBulk(imgsPage.getImages(), param.getDeleteOldImages());
		
		return createImageImportResponse(pageCount, pageNum, imgsPage);
	}
	
	
	
	
	private ProductImageBulkUpdateDTO createImageImportMetaData(IntegrationImageImportDTO param) {
		ProductImageBulkUpdateDTO metadata = new ProductImageBulkUpdateDTO();
		metadata.setPriority(param.getPriority());
		metadata.setType(param.getType());
		metadata.setIgnoreErrors(param.getIgnoreErrors());
		return metadata;
	}






	private ImportedImagesPage importImagesFromExternalSystem(IntegrationImageImportDTO param, Integer pageCount, Integer pageNum) throws InvalidIntegrationEventException {
		ProductImageBulkUpdateDTO metaData = createImageImportMetaData(param);
		//we can't chain the IntegrationUtils.readImgsFromUrls to url-to-product Mono , because this mono is 
		//fetched in another thread, and it doesn't provide security context in other threads 
		//and readImgsFromUrls needs it.
		ImportedImagesUrlMappingPage urlToIdMappingPage = getImgsUrlFromExternalSystem(param, pageCount, pageNum);
		
		return	getImportedImagesPage(urlToIdMappingPage, metaData)
					.defaultIfEmpty(new ImportedImagesPage(0, emptySet()))
					.block(Duration.ofMinutes(PRODUCT_IMPORT_REQUEST_TIMEOUT_MIN));
	}






	private ImportedImagesUrlMappingPage getImgsUrlFromExternalSystem(IntegrationImageImportDTO param,
			Integer pageCount, Integer pageNum) throws InvalidIntegrationEventException {
		ImagesImportEvent event = createImageImportEvent(param, pageCount, pageNum);
		return pushIntegrationEvent(event, this::onImagesImportError)
				.map(EventResult::getReturnedData)
				.blockOptional(Duration.ofMinutes(PRODUCT_IMPORT_REQUEST_TIMEOUT_MIN))
				.orElse(new ImportedImagesUrlMappingPage(0, emptyMap(), WebClient.builder().build()));
	}
	
	
	
	
	private Mono<ImportedImagesPage> getImportedImagesPage(ImportedImagesUrlMappingPage mappingPage, ProductImageBulkUpdateDTO metaData){		
		return utils
				.readImgsFromUrls(mappingPage.getImgToProductsMapping(), metaData, mappingPage.getImgsWebClient())
				.collectList()
				.map(HashSet::new)
				.map(imgs -> new ImportedImagesPage(mappingPage.getTotal(), imgs));
	}






	private ImagesImportEvent createImageImportEvent(IntegrationImageImportDTO param, Integer pageCount,
			Integer pageNum) {
		Long orgId = securityService.getCurrentUserOrganizationId();
		ImageImportParam importParam = new ImageImportParam();
		importParam.setIgnoreErrors(param.getIgnoreErrors());
		importParam.setPageCount(pageCount);
		importParam.setPageNum(pageNum);
		importParam.setPriority(param.getPriority());
		importParam.setType(param.getType());
		
		return Event.ofType(ImagesImportEvent.class)
					.setEventInfo(orgId, importParam)
					.build();
	}






	private ResponsePage<Void> createImageImportResponse(Integer pageCount, Integer pageNum,
			ImportedImagesPage imgsPage) {
		Integer totalElements = imgsPage.getTotalElements();
		ResponsePage<Void> response = new ResponsePage<>();
		response.setContent(emptyList());
		response.setPageNumber(pageNum);
		response.setPageSize(pageCount);
		response.setTotalElements(imgsPage.getTotalElements().longValue());
		response.setTotalPages((int)(totalElements/pageCount));
		return response;
	}
	
	
	
	
	@Override
	public IntegrationUtils getIntegrationUtils() {
		return utils;
	}






	@Override
	public boolean hasActiveIntegration(Long orgId) {
		return ofNullable(modules.getIntegrationInfo(orgId))
				.map(integrationModule -> !integrationModule.isDisabled())
				.orElse(false);
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




@Data
class ProductImportInputData{
	private ProductImportMetadata metadata;
	private List<ProductImportDTO> products;
}




@Data
@AllArgsConstructor
class ImportedImagesPage{
	private Integer totalElements;
	private Set<ImportedImage> images;
}
