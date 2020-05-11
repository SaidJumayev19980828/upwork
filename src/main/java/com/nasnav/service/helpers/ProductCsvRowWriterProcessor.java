package com.nasnav.service.helpers;

import static com.nasnav.exceptions.ErrorCodes.P$EXP$0001;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

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
		return rowData.toArray();
	}

	
	


	private void addFeaturesData(CsvRow input, NormalizedString[] headers, int[] indexesToWrite, List<Object> rowData) {
		if(rowData.size() != headers.length) {
			addPadding(rowData, headers.length);
		}
		
		Map<String,Integer> headerIndices = createHeadersIndicesMap(headers);		
		Map<String,String> features = input.getFeatures();
		
		for(String featureName: features.keySet()) {
			String featureValue = features.get(featureName);
			Integer columnIndex = headerIndices.get(featureName);
			if(Objects.isNull(columnIndex)) {
				throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR, P$EXP$0001, featureName);
			}
			rowData.set(columnIndex, featureValue);
		}
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
