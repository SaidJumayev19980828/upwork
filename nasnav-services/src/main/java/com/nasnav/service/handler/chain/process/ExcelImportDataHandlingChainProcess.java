package com.nasnav.service.handler.chain.process;

import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.handler.HandlingChainingProcess;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.extern.slf4j.Slf4j;

import java.util.List;


@Slf4j
public class ExcelImportDataHandlingChainProcess extends HandlingChainingProcess<ImportDataCommand> {

    public ExcelImportDataHandlingChainProcess(final ImportDataCommand processData, final List<Handler<ImportDataCommand>> handlers) {

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

}
