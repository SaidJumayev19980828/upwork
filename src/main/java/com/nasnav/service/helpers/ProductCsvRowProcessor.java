package com.nasnav.service.helpers;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import com.nasnav.commons.utils.EntityUtils;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.univocity.parsers.common.Context;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.record.Record;

import lombok.AllArgsConstructor;




public class ProductCsvRowProcessor<T extends CsvRow> extends BeanListProcessor<CsvRow> {
	private List<ProductFeaturesEntity> orgVariantFeatures;
	private List<String> defaultTemplateHeaders;
	
	public ProductCsvRowProcessor(Class<CsvRow> beanType, List<ProductFeaturesEntity> orgFeatures , List<String> defaultTemplateHeaders) {
		super(beanType);
		
		this.orgVariantFeatures = ofNullable(orgFeatures).orElse(emptyList());
		this.defaultTemplateHeaders = defaultTemplateHeaders;
	}
	
	
	
	
	
	@Override
	public CsvRow createBean(String[] row, Context context){
		CsvRow bean = super.createBean(row, context);
		if(bean != null) {
			Map<String, String> variantSpec = getVariantFeatureSpecs(row, context);		
			bean.setFeatures(variantSpec);
			
			Map<String, String> extraAttributes = getExtraAttributes(row, context);		
			bean.setExtraAttributes(extraAttributes);
		}
		
		return bean;
	}





	private Map<String, String> getExtraAttributes(String[] row, Context context) {
		Record record = context.toRecord(row);
		Set<String> extraHeaders = getExtraCsvHeaders(context);
		return extraHeaders
				.stream()
				.map(header -> Pair.of(header, record.getString(header)))
				.filter(pair -> pair.getRight() != null)
				.collect(toMap(Pair::getLeft, Pair::getRight));
	}





	private Set<String> getExtraCsvHeaders(Context context) {
		Set<String> extraHeaders = EntityUtils.setOf(context.headers());
		extraHeaders.removeAll(defaultTemplateHeaders);
		return extraHeaders;
	}





	private Map<String, String> getVariantFeatureSpecs(String[] row, Context context) {		
		Record record = context.toRecord(row);
		List<String> headers = asList(context.headers());
		
		return orgVariantFeatures
				.stream()
				.filter(feature -> isFeatureNameInCsvHeaders(headers, feature))
				.map(feature -> new FeatureValuePair(feature, getFeatureValueFromCsvRecord(record, feature)))
				.filter(pair -> pair.value != null)
				.collect(
						toMap(pair -> pair.feature.getName()
							, pair -> pair.value));			
	}





	private Boolean isFeatureNameInCsvHeaders(List<String> headers, ProductFeaturesEntity feature) {
		return headers.contains(feature.getName());
	}
	
	
	
	
	private String getFeatureValueFromCsvRecord(Record record, ProductFeaturesEntity feature) {
		String headerName = feature.getName();
		return record.getString(headerName);
	}

}



@AllArgsConstructor
class FeatureValuePair{
	public ProductFeaturesEntity feature;
	public String value;
}
