package com.nasnav.service.handler;

import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.exceptions.ProcessCancelException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.ImportProcessStatusResponse;
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
public class ImportDataHandlingChainProcessManagerService {

    private final HandlerChainFactory handlerChainFactory;

    Map<String, ImportDataHandlingChainProcess> processes = new HashMap<>();

    ExecutorService executorService = Executors.newFixedThreadPool(2);

    public ImportProcessStatusResponse startProcess(final ImportDataHandlingChainProcess process) {

        processes.put(process.getCurrentStatus().getId(), process);
        executorService.submit(process);
        return ImportProcessStatusResponse.builder()
                .processStatus(process.getCurrentStatus())
                .userId(process.getProcessData().getUserId())
                .orgId(process.getProcessData().getOrgId())
                .build();
    }

    public ImportProcessStatusResponse getProcessStatus(final String processId) {

        validateExistedProcess(processId);
        final ImportDataHandlingChainProcess importDataHandlingChainProcess = processes.get(processId);
        return ImportProcessStatusResponse.builder()
                .processStatus(importDataHandlingChainProcess.getCurrentStatus())
                .orgId(importDataHandlingChainProcess.getProcessData().getOrgId())
                .userId(importDataHandlingChainProcess.getProcessData().getUserId())
                .build();
    }

    public ImportProcessStatusResponse cancelProcess(final String processId) {

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

    public List<ImportProcessStatusResponse> getProcessesStatus() {

        return processes.keySet().stream().map(this::getProcessStatus).collect(Collectors.toList());
    }

    public Object getProcessResult(final String processId) {

        validateExistedProcess(processId);
        return processes.get(processId).getResult();
    }

    public ImportDataHandlingChainProcess createExcelImportDataHandlerChainProcess(ImportDataCommand command) {

        return new ImportDataHandlingChainProcess(command, handlerChainFactory.importExcelDataHandlerChain());
    }

    public void clearAllProcess() {

        clearAllProcess(processes.keySet().stream().collect(Collectors.toList()));
    }

    public void clearProcess(final String processId) {

        validateExistedProcess(processId);
        if (processes.get(processId).getCurrentStatus().isNotValidToClear())
            throw new RuntimeBusinessException("process with id " + processId + " still in progress", "FIND PROCESS FAILED", HttpStatus.NOT_FOUND);
        processes.remove(processId);
    }

    public ImportDataHandlingChainProcess createCsvImportDataHandlerChainProcess(final ImportDataCommand command) {

        return new ImportDataHandlingChainProcess(command, handlerChainFactory.importCsvDataHandlerChain());
    }

    public List<ImportProcessStatusResponse> getProcessesStatus(final List<String> processIds) {

        return processIds.stream().filter(id -> processes.containsKey(id))
                .map(this::getProcessStatus)
                .collect(Collectors.toList());
    }

    public void clearAllProcess(final List<String> processIds) {

        processIds.stream().filter(id -> processes.containsKey(id)
                        && !processes.get(id).getCurrentStatus().isNotValidToClear())
                .forEach(this::clearProcess);
    }

}
