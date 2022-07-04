package com.nasnav.commons.model.handler;


import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.univocity.parsers.common.DataProcessingException;
import com.univocity.parsers.common.ParsingContext;
import com.univocity.parsers.common.RowProcessorErrorHandler;
import lombok.Data;

import java.util.Arrays;

@Data
public class RowParseErrorHandler implements RowProcessorErrorHandler {
    private ImportProductContext importContext;

    public RowParseErrorHandler(ImportProductContext context) {
        this.importContext = context;
    }

    @Override
    public void handleError(DataProcessingException error, Object[] inputRow, ParsingContext context) {
        importContext.logNewError(error, Arrays.toString(inputRow), (int)(context.currentLine())+1);
    }
}
