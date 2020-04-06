package com.nasnav.service.helpers;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.integration.IntegrationService;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;

import lombok.AllArgsConstructor;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple3;

@Service
public class CachingHelper {
	
	
	private static final int SEARCH_BATCH_SIZE = 1000;


	@Autowired
	SecurityService securityService;
	
	
	@Autowired
	ProductVariantsRepository productVariantsRepository;
	
	
	@Autowired
	IntegrationService integrationService;
	
	
	public VariantCache createVariantCache(List<VariantIdentifier> variantIdentifiers) {
		return createVariantCacheMono(variantIdentifiers).block();
	}
	
	

	
	
	
	public  Mono<VariantCache> createVariantCacheMono(List<VariantIdentifier> variantIdentifiers) {
		Mono<Map<String, ProductVariantsEntity>> idToVariantMap = getIdToVariantMap(variantIdentifiers);
		Mono<Map<String, ProductVariantsEntity>> externalIdToVariantMap = getExternalIdToVariantMap(variantIdentifiers);
		Mono<Map<String, ProductVariantsEntity>> barcodeToVariantMap = getBarcodeToVariantMap(variantIdentifiers);
		
		return Mono.zip(idToVariantMap, externalIdToVariantMap, barcodeToVariantMap)
				.map(this::createVariantCacheFromTuple);
	}
	
	
	
	
	public Optional<ProductVariantsEntity> getVariantFromCache(VariantIdentifier identifiers, VariantCache cache) {
		ProductVariantsEntity variant= 
			ofNullable(cache.getIdToVariantMap().get(identifiers.getVariantId()))
				.orElse(
					ofNullable(cache.getExternalIdToVariantMap().get(identifiers.getExternalId()))
						.orElse(
							cache.getBarcodeToVariantMap().get(identifiers.getBarcode())));
		return ofNullable(variant);			
	}
	
	
	
	
	private VariantCache createVariantCacheFromTuple(Tuple3<Map<String, ProductVariantsEntity>, Map<String, ProductVariantsEntity>, Map<String, ProductVariantsEntity>> tuple) {
		Map<String, ProductVariantsEntity> idToVariantMap = tuple.getT1();
		Map<String, ProductVariantsEntity> externalIdToVariantMap = tuple.getT2();
		Map<String, ProductVariantsEntity> barcodeToVariantMap = tuple.getT3();
		return new VariantCache(idToVariantMap, externalIdToVariantMap, barcodeToVariantMap);
	}
	
	
	
	
	
	private Mono<Map<String, ProductVariantsEntity>> getIdToVariantMap(
			List<VariantIdentifier> variantIdentifiers) {
		return 
			Flux
			.fromIterable(variantIdentifiers)	
			.filter(identifier -> isNotBlankOrNull(identifier.getVariantId()))
			.map(VariantIdentifier::getVariantId)
			.window(1000)
			.flatMap(this::getVariantsById)
			.collectMap(this::getIdAsString, variant -> variant);
	}

	
	
	
	
	
	private Flux<ProductVariantsEntity> getVariantsById(Flux<String> idList){
		return 
			idList
			.map(Long::valueOf)
			.buffer()
			.flatMapIterable(productVariantsRepository::findByIdIn);
	}
	
	
	
	
	
	private Flux<ProductVariantsEntityWithExternalId> getVariantsByExternalId(Flux<String> idList){		
		return 
			idList
			.buffer()
			.flatMapIterable(this::getProductVariantFromExternalIdIn);
	}
	
	
	
	
	
	private Flux<ProductVariantsEntity> getVariantsByBarcode(Flux<String> barcodeList){
		Long orgId = securityService.getCurrentUserOrganizationId();
		return 
			barcodeList
			.buffer()
			.flatMapIterable(barcodes -> productVariantsRepository.findByOrganizationIdAndBarcodeIn(orgId, barcodes));
	}
	
	
	
	
	private List<ProductVariantsEntityWithExternalId> getProductVariantFromExternalIdIn(List<String> extIdList){
		Long orgId = securityService.getCurrentUserOrganizationId();
		Map<String,String> mapping = integrationService.getLocalMappedValues(orgId, PRODUCT_VARIANT, extIdList);
		Map<String,String> localToExtIdMapping = 
				mapping
				.entrySet()
				.stream()
				.collect(toMap(Map.Entry::getValue, Map.Entry::getKey));
		
		List<Long> variantIds = 
				localToExtIdMapping
				.keySet()
				.stream()
				.map(Long::valueOf)
				.collect(toList());
		
		return 
			productVariantsRepository
				.findByIdIn(variantIds)
				.stream()
				.map(variant -> toProductVariantEntityWithExtId(localToExtIdMapping, variant))
				.collect(toList());
	}
	
	
	
	
	private ProductVariantsEntityWithExternalId toProductVariantEntityWithExtId(Map<String, String> localToExtIdMapping,
			ProductVariantsEntity variant) {
		String extId = localToExtIdMapping.get(String.valueOf(variant.getId()));
		return new ProductVariantsEntityWithExternalId(variant , extId);
	}
	
	
	
	
	private String getIdAsString(ProductVariantsEntity variant) {
		return String.valueOf(variant.getId());
	}

	
	
	
	private Mono<Map<String, ProductVariantsEntity>> getBarcodeToVariantMap(
			List<VariantIdentifier> variantIdentifiers) {
		return 
			Flux
			.fromIterable(variantIdentifiers)	
			.filter(identifier -> isNotBlankOrNull(identifier.getBarcode()))
			.map(VariantIdentifier::getBarcode)
			.window(SEARCH_BATCH_SIZE)
			.flatMap(this::getVariantsByBarcode)
			.collectMap(ProductVariantsEntity::getBarcode, variant -> variant);
	}






	private Mono<Map<String, ProductVariantsEntity>> getExternalIdToVariantMap(
			List<VariantIdentifier> variantIdentifiers) {
		return 
			Flux
			.fromIterable(variantIdentifiers)	
			.filter(identifier -> isNotBlankOrNull(identifier.getExternalId()))
			.map(VariantIdentifier::getExternalId)
			.window(1000)
			.flatMap(this::getVariantsByExternalId)
			.collectMap(variant-> variant.externalId, variant -> variant.variant);
	}




}






@AllArgsConstructor
class ProductVariantsEntityWithExternalId {
	public ProductVariantsEntity variant;
	public String externalId;
}

