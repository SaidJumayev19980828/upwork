package com.nasnav.service.helpers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;

import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.CsvRow;
import com.univocity.parsers.common.Context;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.record.Record;




public class ProductCsvRowProcessor<T extends CsvRow> extends BeanListProcessor<CsvRow> {
	List<ProductFeaturesEntity> orgVariantFeatures;
	
	
	public ProductCsvRowProcessor(Class<CsvRow> beanType, List<ProductFeaturesEntity> orgFeatures) {
		super(beanType);
		
		this.orgVariantFeatures = ofNullable(orgFeatures).orElse(emptyList());
	}
	
	
	
	
	
	@Override
	public CsvRow createBean(String[] row, Context context){
		CsvRow bean = super.createBean(row, context);
		if(bean != null) {
			Map<String, String> variantSpec = getVariantFeatureSpecs(row, context);		
			bean.setFeatures(variantSpec);
		}
		
		return bean;
	}





	private Map<String, String> getVariantFeatureSpecs(String[] row, Context context) {		
		Record record = context.toRecord(row);
		List<String> headers = asList(context.headers());
		
		return orgVariantFeatures
				.stream()
				.filter(feature -> isFeatureNameInCsvHeaders(headers, feature))
				.collect(toMap(this::getFeatureIdAsStr, feature -> getFeatureValueFromCsvRecord(record, feature)));			
	}





	private Boolean isFeatureNameInCsvHeaders(List<String> headers, ProductFeaturesEntity feature) {
		return headers.contains(feature.getName());
	}
	
	
	
	
	
	private String getFeatureIdAsStr(ProductFeaturesEntity feature) {
		return String.valueOf(feature.getId());
	}
	
	
	
	
	
	private String getFeatureValueFromCsvRecord(Record record, ProductFeaturesEntity feature) {
		String headerName = feature.getName();
		return record.getString(headerName);
	}

}
