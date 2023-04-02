package com.nasnav.service;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.response.ImportProcessStatusResponse;
import com.nasnav.service.handler.ImportDataHandlingChainProcessManagerService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class DataImportAsyncServiceImpl {

    // both functions can be different implementations to the same function. Also, file types should be checked here

    private final ImportDataHandlingChainProcessManagerService handlingChainingProcessManagerService;

    @Transactional
    public ImportProcessStatusResponse importExcelProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData, Long orgId, Long userId) throws Exception {

        final byte[] bytes = IOUtils.toByteArray(file.getInputStream());

        return handlingChainingProcessManagerService.startProcess(
                handlingChainingProcessManagerService.createExcelImportDataHandlerChainProcess(ImportDataCommand.builder()
                        .importMetaDataDto(importMetaData)
                        .file(bytes)
                        .orgId(orgId)
                        .userId(userId)
                        .build()));
    }

    @Transactional
    public ImportProcessStatusResponse importCsvProductList(final MultipartFile file, final ProductListImportDTO importMetaData, Long orgId, Long userId) throws Exception {

        final byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        return handlingChainingProcessManagerService.startProcess(
                handlingChainingProcessManagerService.createCsvImportDataHandlerChainProcess(ImportDataCommand.builder()
                        .importMetaDataDto(importMetaData)
                        .file(bytes)
                        .orgId(orgId)
                        .userId(userId)
                        .build()));
    }

}




