package com.nasnav.service.handler;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.exceptions.ProcessCancelException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.service.handler.chain.process.ImportDataHandlingChainProcess;
import com.nasnav.service.handler.dataimport.HandlerChainFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class HandlingChainingProcessManagerService {

    private final HandlerChainFactory handlerChainFactory;

    Map<String, HandlingChainingProcess<?>> processes = new HashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    public HandlerChainProcessStatus startProcess(final HandlingChainingProcess<?> process) {

        processes.put(process.getCurrentStatus().getId(), process);
        executorService.submit(process);
        return process.getCurrentStatus();
    }

    public HandlerChainProcessStatus getProcessStatus(final String processId) {

        validateExistedProcess(processId);
        return processes.get(processId).getCurrentStatus();
    }

    public HandlerChainProcessStatus cancelProcess(final String processId) {

        validateExistedProcess(processId);
        try {
            processes.get(processId).cancelProcess();
            return getProcessStatus(processId);
        } catch (ProcessCancelException e) {
            throw new RuntimeBusinessException(e.getMessage(), "CANCEL PROCESS FAILED", HttpStatus.BAD_REQUEST, e);
        }
    }

    private void validateExistedProcess(final String processId) {

        if (!processes.containsKey(processId))
            throw new RuntimeBusinessException("process with id " + processId + " not found", "FIND PROCESS FAILED", HttpStatus.NOT_FOUND);
    }

    public List<HandlerChainProcessStatus> getProcessesStatus() {

        return processes.values().stream().map(HandlingChainingProcess::getCurrentStatus).collect(Collectors.toList());
    }

    public Object getProcessResult(final String processId) {

        validateExistedProcess(processId);
        return processes.get(processId).getResult();
    }

    public ImportDataHandlingChainProcess createExcelImportDataHandlerChainProcess(ImportDataCommand command) {

        return new ImportDataHandlingChainProcess(command, handlerChainFactory.importExcelDataHandlerChain());
    }

    public void clearAllProcess() {

        processes.clear();
    }

    public void clearProcess(final String processId) {

        validateExistedProcess(processId);
        processes.remove(processId);
    }

    public ImportDataHandlingChainProcess createCsvImportDataHandlerChainProcess(final ImportDataCommand command) {

        return new ImportDataHandlingChainProcess(command, handlerChainFactory.importCsvDataHandlerChain());
    }

    public List<HandlerChainProcessStatus> getProcessesStatus(final List<String> processIds) {

        return processIds.stream().filter(id -> processes.containsKey(id))
                .map(this::getProcessStatus)
                .collect(Collectors.toList());
    }

    public void clearAllProcess(final List<String> processIds) {

        processIds.stream().filter(id -> processes.containsKey(id)).forEach(this::clearProcess);
    }

}
