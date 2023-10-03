package com.nasnav.service.handler.chain.process;

import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.handler.HandlingChainingProcess;
import com.nasnav.service.model.importproduct.context.Error;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class ImportDataHandlingChainProcess extends HandlingChainingProcess<ImportDataCommand> {

    public ImportDataHandlingChainProcess(final ImportDataCommand processData, final List<Handler<ImportDataCommand>> handlers) {

        super(processData, handlers);
    }

    @Override
    public void handleCancelProcess() {
        log.info("TODO handleCancelProcess");
    }

    @Override
    public ImportProductContext getResult() {

        return getProcessData().getContext();
    }

    @Override
    protected void handleException(Exception ex) {
        if (ex instanceof ImportProductException) {
            getProcessData().setContext(((ImportProductException)ex).getContext());
        } else {
            final ProductImportMetadata metadata = getProcessData().getImportMetadata();
            final ImportProductContext context = new ImportProductContext();
            context.setImportMetaData(metadata);
            context.getErrors().add(new Error(ex.getMessage(), null));
            getProcessData().setContext(context);
        }
    }

    

}
