package com.nasnav.service.model.importproduct.csv;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.CollectionUtils;
import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static java.util.Collections.emptySet;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toSet;

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
	protected String productGroupKey;
	protected BigDecimal discount;
	protected Long productId;
	protected String sku;
	protected String productCode;
	protected String unit;
	protected BigDecimal weight;
	protected String imagePath;

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
		product.setExtraAttributes(getExtraAttributes());
		product.setProductGroupKey(productGroupKey);
		product.setDiscount(discount);
		product.setSku(sku);
		product.setProductCode(productCode);
		product.setUnit(unit);
		product.setWeight(weight);

		return product;
	}




	private Set<String> parseTags() {
		return	ofNullable(tags)
				.map(tagStr -> tagStr.split(TAGS_SEPARATOR))
				.map(CollectionUtils::setOf)
				.orElse(emptySet())
				.stream()
				.map(tagStr -> tagStr.trim())
				.collect(toSet());
	}
}