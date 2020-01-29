package com.nasnav.service;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.EntityUtils;

import lombok.Data;

@Data
public class CsvRow {
	private static final String TAGS_SEPARATOR = ";";
	protected Long variantId;
	protected String externalId;
	protected String name;
	protected String description;
	protected String barcode;
	protected String tags;
	protected String brand;
	protected Integer quantity;
	protected BigDecimal price;
	protected Map<String,String> features;
	protected Map<String,String> extraAttributes;
	
	public CsvRow() {
		extraAttributes = new HashMap<>();
		features = new HashMap<>();
	}
	
	
	
	public ProductImportDTO toProductImportDto() {
		ProductImportDTO product = new ProductImportDTO();
		
		Set<String> tagsSet = parseTags();
		
		product.setBarcode(barcode);
		product.setBrand(brand);
		product.setDescription(description);
		product.setExternalId(externalId);
		product.setFeatures(getFeatures());
		product.setName(name);
		product.setPrice(price);
		product.setQuantity(quantity);
		product.setTags(tagsSet);
		product.setVariantId(variantId);
		
		return product;
	}

	


	private Set<String> parseTags() {
		return	ofNullable(tags)
				.map(tagStr -> tagStr.split(TAGS_SEPARATOR))
				.map(EntityUtils::setOf)
				.orElse(emptySet())
				.stream()
				.map(tagStr -> tagStr.trim())
				.collect(toSet());
	}
}