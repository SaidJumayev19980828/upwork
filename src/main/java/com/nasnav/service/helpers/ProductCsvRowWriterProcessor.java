package com.nasnav.service.helpers;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.P$EXP$0001;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.univocity.parsers.common.NormalizedString;
import com.univocity.parsers.common.processor.BeanWriterProcessor;

public class ProductCsvRowWriterProcessor extends BeanWriterProcessor<CsvRow>{

	public ProductCsvRowWriterProcessor(Class<CsvRow> beanType) {
		super(beanType);
	}
	
	
	
	@Override
	public Object[] write(CsvRow input, NormalizedString[] headers, int[] indexesToWrite) {
		List<Object> rowData = asList(super.write(input, headers, indexesToWrite));
		addFeaturesData(input, headers, indexesToWrite, rowData);
		addExtraAttributesData(input, headers, indexesToWrite, rowData);
		return rowData.toArray();
	}

	
	


	private void addExtraAttributesData(CsvRow input, NormalizedString[] headers, int[] indexesToWrite,
			List<Object> rowData) {
		Map<String,String> attributes = input.getExtraAttributes();		
		insertAdditionalDataToRow(headers, rowData, attributes);
	}


	

	private void addFeaturesData(CsvRow input, NormalizedString[] headers, int[] indexesToWrite, List<Object> rowData) {
		Map<String,String> features = input.getFeatures();		
		insertAdditionalDataToRow(headers, rowData, features);
	}


	

	private void insertAdditionalDataToRow(NormalizedString[] headers, List<Object> rowData,
			Map<String, String> additionalData) {
		if(rowData.size() != headers.length) {
			addPadding(rowData, headers.length);
		}
		
		Map<String,Integer> headerIndices = createHeadersIndicesMap(headers);		
		
		additionalData
		.entrySet()
		.stream()
		.filter(data -> isNotBlankOrNull(data.getKey()))
		.forEach(data -> addToRow(data, headerIndices, rowData));
	}



	private void addToRow(Entry<String, String> data, Map<String, Integer> headerIndices, List<Object> rowData) {
		String key = data.getKey();
		String value = data.getValue();	
		Integer columnIndex = 
				ofNullable(headerIndices.get(key))
				.orElseThrow(() -> new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$EXP$0001, key));
		rowData.set(columnIndex, value);
	}



	private Map<String, Integer> createHeadersIndicesMap(NormalizedString[] headers) {
		Map<String, Integer> map = new HashMap<>();
		for(int i =0; i< headers.length; i++) {
			int index = i;
			ofNullable(headers[i])
			.map(NormalizedString::toLiteral)
			.map(NormalizedString::toString)
			.ifPresent(header -> map.put(header, index));
		}
		return map;
	}



	private void addPadding(List<Object> rowData, int length) {
		int paddingLength = length - rowData.size();
		Object[] padding = new Object[paddingLength];
		rowData.addAll(asList(padding));
	}

}
