package com.nasnav.service.handler;

import com.nasnav.commons.model.handler.*;
import com.nasnav.dao.*;
import com.nasnav.dto.request.notification.PushMessageDTO;
import com.nasnav.enumerations.NotificationType;
import com.nasnav.exceptions.*;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.service.handler.chain.process.ImportDataHandlingChainProcess;
import com.nasnav.service.handler.dataimport.HandlerChainFactory;
import com.nasnav.service.notification.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

import static com.nasnav.exceptions.ErrorCodes.U$0001;

@Service
@Slf4j
@RequiredArgsConstructor
public class ImportDataHandlingChainProcessManagerService {

    private final HandlerChainFactory handlerChainFactory;
    private final NotificationService notificationService;
    private final UserRepository userRepository;
    private final EmployeeUserRepository employeeUserRepository;

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

        return new ImportDataHandlingChainProcess(command, handlerChainFactory.importExcelDataHandlerChain(), this::onFinishingProcess);
    }

    private void onFinishingProcess(String processId) {
        Long userId = processes.get(processId).getProcessData().getUserId();
        BaseUserEntity user = employeeUserRepository.findById(userId).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND, U$0001, userId));
        HandlerChainProcessStatus processStatus = getProcessStatus(processId).getProcessStatus();
        JSONObject bodyJSON = new JSONObject().put("Process Id", processId).put("Status", processStatus.getStatus());
        if (processStatus.isFailed()) {
            bodyJSON.put("Result", getProcessResult(processId));
        }
        PushMessageDTO<Object> messageDTO = new PushMessageDTO<>("Importing Data Process Update", bodyJSON.toString(),
                NotificationType.IMPORT_DATA_PROCESS_UPDATE);
        notificationService.sendMessage(user, messageDTO);
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

        return new ImportDataHandlingChainProcess(command, handlerChainFactory.importCsvDataHandlerChain(), this::onFinishingProcess);
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
