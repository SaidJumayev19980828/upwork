package com.nasnav.service.helpers;

import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.model.importproduct.csv.OrderRow;
import com.univocity.parsers.common.NormalizedString;
import com.univocity.parsers.common.processor.BeanWriterProcessor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import static com.nasnav.exceptions.ErrorCodes.P$EXP$0001;
import static java.util.Arrays.asList;
import static java.util.Optional.ofNullable;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

public class OrderCsvRowWriterProcessor extends BeanWriterProcessor<DetailedOrderRepObject> {
    public OrderCsvRowWriterProcessor(Class<DetailedOrderRepObject> beanType) {
        super(beanType);
    }



    @Override
    public Object[] write(DetailedOrderRepObject input, NormalizedString[] headers, int[] indexesToWrite) {
        List<Object> rowData = asList(super.write(input, headers, indexesToWrite));
        return rowData.toArray();
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



    private void addToRow(Map.Entry<String, String> data, Map<String, Integer> headerIndices, List<Object> rowData) {
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

