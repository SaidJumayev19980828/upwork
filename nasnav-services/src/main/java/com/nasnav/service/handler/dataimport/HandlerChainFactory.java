package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.service.handler.Handler;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class HandlerChainFactory {

    private final Map<String, Handler<ImportDataCommand>> importDataHandlers;

    public static final String VALIDATE_PRODUCT_IMPORT_DATA_INPUT = "VALIDATE_PRODUCT_IMPORT_DATA_INPUT";

    public static final String PARSE_EXCEL_PRODUCT_IMPORT_DATA = "PARSE_EXCEL_PRODUCT_IMPORT_DATA";

    public static final String INIT_CONTEXT_PRODUCT_IMPORT_DATA = "INIT_CONTEXT_PRODUCT_IMPORT_DATA";

    public static final String CREATE_CACHE_PRODUCT_IMPORT_DATA = "CREATE_CACHE_PRODUCT_IMPORT_DATA";

    public static final String VALIDATE_PRODUCT_IMPORT_DATA = "VALIDATE_PRODUCT_IMPORT_DATA";

    public static final String PRODUCT_DATA_LIST_CONVERTER_IMPORT_DATA = "PRODUCT_DATA_LIST_CONVERTER_IMPORT_DATA";

    public static final String SAVE_PRODUCT_IMPORT_DATA = "SAVE_PRODUCT_IMPORT_DATA";

    public static final String DELETE_OLD_PRODUCT_IMPORT_DATA = "DELETE_OLD_PRODUCT_IMPORT_DATA";


    public List<Handler<ImportDataCommand>> importDataHandlerChain() {

        return Stream.of(importDataHandlers.get(VALIDATE_PRODUCT_IMPORT_DATA_INPUT)
                , importDataHandlers.get(PARSE_EXCEL_PRODUCT_IMPORT_DATA)
                , importDataHandlers.get(INIT_CONTEXT_PRODUCT_IMPORT_DATA)
                , importDataHandlers.get(CREATE_CACHE_PRODUCT_IMPORT_DATA)
                , importDataHandlers.get(VALIDATE_PRODUCT_IMPORT_DATA)
                , importDataHandlers.get(PRODUCT_DATA_LIST_CONVERTER_IMPORT_DATA)
                , importDataHandlers.get(SAVE_PRODUCT_IMPORT_DATA)
                , importDataHandlers.get(DELETE_OLD_PRODUCT_IMPORT_DATA)
        ).collect(Collectors.toList());
    }

}
