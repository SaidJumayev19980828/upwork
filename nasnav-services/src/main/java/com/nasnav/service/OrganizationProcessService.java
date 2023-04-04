package com.nasnav.service;

import com.nasnav.commons.utils.FilesUtils;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.DataImportAsyncException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.service.handler.ImportDataHandlingChainProcessManagerService;
import com.nasnav.service.impl.DataImportAsyncServiceImpl;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class OrganizationProcessService {

    Map<Long, List<String>> orgProcesses = new HashMap<>();

    private final SecurityService security;

    private final ImportDataHandlingChainProcessManagerService handlingChainingProcessManagerService;

    private final DataImportAsyncServiceImpl dataImportAsyncService;

    public List<ImportProcessStatusResponse> getProcessesStatus() {

        final Long orgId = security.getCurrentUserOrganizationId();
        validateExistedProcessesForOrganization(orgId);
        return handlingChainingProcessManagerService.getProcessesStatus(orgProcesses.get(orgId));
    }

    public ImportProcessStatusResponse getProcessStatus(final String processId) {

        final Long orgId = security.getCurrentUserOrganizationId();
        validateExistedProcessesForOrganization(orgId, processId);
        return handlingChainingProcessManagerService.getProcessStatus(processId);
    }

    public Object getProcessResult(final String processId) {

        final Long orgId = security.getCurrentUserOrganizationId();
        validateExistedProcessesForOrganization(orgId, processId);
        return handlingChainingProcessManagerService.getProcessResult(processId);
    }

    public ImportProcessStatusResponse cancelProcess(final String processId) {

        final Long orgId = security.getCurrentUserOrganizationId();
        validateExistedProcessesForOrganization(orgId, processId);
        return handlingChainingProcessManagerService.cancelProcess(processId);
    }

    public void clearProcess(final String processId) {

        final Long orgId = security.getCurrentUserOrganizationId();
        validateExistedProcessesForOrganization(orgId, processId);
        handlingChainingProcessManagerService.clearProcess(processId);
        orgProcesses.get(orgId).remove(processId);
    }

    public void clearAllProcess() {

        final Long orgId = security.getCurrentUserOrganizationId();
        validateExistedProcessesForOrganization(orgId);
        handlingChainingProcessManagerService.clearAllProcess(orgProcesses.get(orgId));
        orgProcesses.remove(orgId);
    }

    private void validateExistedProcessesForOrganization(final Long orgId) {

        if (!orgProcesses.containsKey(orgId) || CollectionUtils.isEmpty(orgProcesses.get(orgId)))
            throw new RuntimeBusinessException("Organization with id " + orgId + " not have Processes ", "FIND PROCESS FAILED", HttpStatus.NOT_FOUND);
    }

    private void validateExistedProcessesForOrganization(final Long orgId, final String processId) {

        validateExistedProcessesForOrganization(orgId);
        if (!orgProcesses.get(orgId).contains(processId))
            throw new RuntimeBusinessException("Organization with id " + orgId + " not have Process with id " + processId,
                    "FIND PROCESS FAILED", HttpStatus.NOT_FOUND);
    }



    public ImportProcessStatusResponse importExcelProductList(final MultipartFile file, final ProductListImportDTO importMetaData) throws Exception {
        ImportProcessStatusResponse response = null;
        if (FilesUtils.isExcel(file)) {
            final Long orgId = security.getCurrentUserOrganizationId();
            final Long userId = security.getCurrentUser().getId();
            response = dataImportAsyncService.importExcelProductList(file, importMetaData, orgId, userId);
            connectOrganizationWithProcess(orgId, response.getProcessStatus().getId());
        }
        if (response != null && ( response.getProcessStatus().isInProgress() || response.getProcessStatus().isSuccess())) {
            return response;
        } else {
            throw new DataImportAsyncException(response);
        }
    }

    private void connectOrganizationWithProcess(Long orgId, String processId) {

        if (!orgProcesses.containsKey(orgId))
            orgProcesses.put(orgId, new ArrayList<>());

        orgProcesses.get(orgId).add(processId);
    }

    public ImportProcessStatusResponse importCsvProductList(final MultipartFile file, final ProductListImportDTO importMetaData) throws Exception {
        ImportProcessStatusResponse response = null;
        if (FilesUtils.isCsv(file)) {
            final Long orgId = security.getCurrentUserOrganizationId();
            final Long userId = security.getCurrentUser().getId();
            response = dataImportAsyncService.importCsvProductList(file, importMetaData, orgId, userId);
            connectOrganizationWithProcess(orgId, response.getProcessStatus().getId());
        }
        if (response != null && ( response.getProcessStatus().isInProgress() || response.getProcessStatus().isSuccess())) {
            return response;
        } else {
            throw new DataImportAsyncException(response);
        }
        
    }

}
