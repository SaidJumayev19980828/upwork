package com.nasnav.service.helpers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.univocity.parsers.common.Context;
import com.univocity.parsers.common.processor.BeanListProcessor;
import com.univocity.parsers.common.record.Record;




public class ProductCsvRowProcessor<T extends ProductImportDTO> extends BeanListProcessor<ProductImportDTO> {
	List<ProductFeaturesEntity> features;
	
	
	public ProductCsvRowProcessor(Class<ProductImportDTO> beanType, List<ProductFeaturesEntity> features) {
		super(beanType);
		
		this.features = features != null ? features : new ArrayList<>();
	}
	
	
	
	
	
	@Override
	public ProductImportDTO createBean(String[] row, Context context){
		ProductImportDTO bean = super.createBean(row, context);
		if(bean != null) {
			Map<String, String> spec = getFeatureSpecs(row, context);		
			bean.setFeatures(spec);
		}
		
		return bean;
	}





	private Map<String, String> getFeatureSpecs(String[] row, Context context) {
		Map<String, String> spec = new HashMap<>();
		
		Record record = context.toRecord(row);
		List<String> headers = Arrays.asList(context.headers());
		this.features.stream()
				.filter(f -> headers.contains(f.getName()))
				.forEach(
						f -> spec.put( String.valueOf(f.getId())
								, record.getString(f.getName())));
		return spec;
	}
	
	

}
