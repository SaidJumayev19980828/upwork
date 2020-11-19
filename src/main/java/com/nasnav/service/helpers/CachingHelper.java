package com.nasnav.service.helpers;

import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.integration.enums.MappingType.PRODUCT_VARIANT;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.integration.IntegrationServiceHelper;
import com.nasnav.service.SecurityService;
import com.nasnav.service.model.VariantBasicData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
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
	IntegrationServiceHelper integrationHelper;
	
	
	public VariantCache createVariantCache(List<VariantIdentifier> variantIdentifiers) {
		return createVariantCacheMono(variantIdentifiers).block();
	}
	
	

	
	
	
	public  Mono<VariantCache> createVariantCacheMono(List<VariantIdentifier> variantIdentifiers) {
		Mono<Map<String, VariantBasicData>> idToVariantMap = getIdToVariantMap(variantIdentifiers);
		Mono<Map<String, VariantBasicData>> externalIdToVariantMap = getExternalIdToVariantMap(variantIdentifiers);
		Mono<Map<String, VariantBasicData>> barcodeToVariantMap = getBarcodeToVariantMap(variantIdentifiers);
		
		return Mono.zip(idToVariantMap, externalIdToVariantMap, barcodeToVariantMap)
				.map(this::createVariantCacheFromTuple);
	}
	
	
	
	
	public Optional<VariantBasicData> getVariantFromCache(VariantIdentifier identifiers, VariantCache cache) {
		VariantBasicData variant= 
			ofNullable(cache.getIdToVariantMap().get(identifiers.getVariantId()))
				.orElse(
					ofNullable(cache.getExternalIdToVariantMap().get(identifiers.getExternalId()))
						.orElse(
							cache.getBarcodeToVariantMap().get(identifiers.getBarcode())));
		return ofNullable(variant);			
	}
	
	
	
	
	private VariantCache createVariantCacheFromTuple(Tuple3<Map<String, VariantBasicData>, Map<String, VariantBasicData>, Map<String, VariantBasicData>> tuple) {
		Map<String, VariantBasicData> idToVariantMap = tuple.getT1();
		Map<String, VariantBasicData> externalIdToVariantMap = tuple.getT2();
		Map<String, VariantBasicData> barcodeToVariantMap = tuple.getT3();
		return new VariantCache(idToVariantMap, externalIdToVariantMap, barcodeToVariantMap);
	}
	
	
	
	
	
	private Mono<Map<String, VariantBasicData>> getIdToVariantMap(
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

	
	
	
	
	
	private Flux<VariantBasicData> getVariantsById(Flux<String> idList){
		return 
			idList
			.map(Long::valueOf)
			.buffer()
			.filter(variantIdList -> !variantIdList.isEmpty())
			.defaultIfEmpty(asList(-1L))
			.flatMapIterable(productVariantsRepository::findVariantBasicDataByIdIn);
	}
	
	
	
	
	
	private Flux<Variant> getVariantsByExternalId(Flux<String> idList){		
		return 
			idList
			.buffer()
			.filter(exIdList -> !exIdList.isEmpty())
			.defaultIfEmpty(asList(randomUUID().toString())) //the list should never be empty
			.flatMapIterable(this::getProductVariantFromExternalIdIn);
	}
	
	
	
	
	
	private Flux<VariantBasicData> getVariantsByBarcode(Flux<String> barcodeList){
		Long orgId = securityService.getCurrentUserOrganizationId();
		return 
			barcodeList
			.buffer()
			.filter(barcodes -> !barcodes.isEmpty())
			.defaultIfEmpty(asList(randomUUID().toString())) //the list should never be empty
			.flatMapIterable(
							barcodes -> mapInBatches(barcodes,
											500,
													 barcode -> productVariantsRepository.findByOrganizationIdAndBarcodeIn(orgId, barcode)));
	}
	
	
	
	
	private List<Variant> getProductVariantFromExternalIdIn(List<String> extIdList){
		Long orgId = securityService.getCurrentUserOrganizationId();
		Map<String,String> mapping = integrationHelper.getLocalMappedValues(orgId, PRODUCT_VARIANT, extIdList);
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
		
		return 	findVariantsbyId(localToExtIdMapping, variantIds);
	}






	private List<Variant> findVariantsbyId(Map<String, String> localToExtIdMapping, List<Long> variantIds) {
		List<Long> idList = new ArrayList<>();
		idList.add(-1L);//the list should be non-empty
		idList.addAll(variantIds);
		return productVariantsRepository
				.findVariantBasicDataByIdIn(idList)
				.stream()
				.map(variant -> toVariant(localToExtIdMapping, variant))
				.collect(toList());
	}
	
	
	
	
	private Variant toVariant(Map<String, String> localToExtIdMapping,
			VariantBasicData variant) {
		String extId = localToExtIdMapping.get(String.valueOf(variant.getVariantId()));
		return new Variant(variant , extId);
	}
	
	
	
	
	private String getIdAsString(VariantBasicData variant) {
		return String.valueOf(variant.getVariantId());
	}

	
	
	
	private Mono<Map<String, VariantBasicData>> getBarcodeToVariantMap(
			List<VariantIdentifier> variantIdentifiers) {
		return 
			Flux
			.fromIterable(variantIdentifiers)	
			.filter(identifier -> isNotBlankOrNull(identifier.getBarcode()))
			.map(VariantIdentifier::getBarcode)
			.window(SEARCH_BATCH_SIZE)
			.flatMap(this::getVariantsByBarcode)
			.collectMap(VariantBasicData::getBarcode, variant -> variant);
	}






	private Mono<Map<String, VariantBasicData>> getExternalIdToVariantMap(
			List<VariantIdentifier> variantIdentifiers) {
		return 
			Flux
			.fromIterable(variantIdentifiers)	
			.filter(identifier -> isNotBlankOrNull(identifier.getExternalId()))
			.map(VariantIdentifier::getExternalId)
			.window(1000)
			.flatMap(this::getVariantsByExternalId)
			.collectMap(variant-> variant.getExternalId(), variant -> variant);
	}




}






@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
class Variant extends VariantBasicData {
	private String externalId;
	
	public Variant(VariantBasicData basicData, String externalId) {
		this.setVariantId(basicData.getVariantId());
		this.setProductId(basicData.getProductId());
		this.setOrganizationId(basicData.getOrganizationId());
		this.externalId = externalId;
	}
}

