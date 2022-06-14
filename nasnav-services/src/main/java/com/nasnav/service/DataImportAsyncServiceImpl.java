package com.nasnav.service;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.service.handler.HandlingChainingProcessManagerService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.io.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Service
@RequiredArgsConstructor
public class DataImportAsyncServiceImpl {

    private final HandlingChainingProcessManagerService handlingChainingProcessManagerService;

    @Transactional
    public HandlerChainProcessStatus importExcelProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData, Long orgId) throws Exception {

        final byte[] bytes = IOUtils.toByteArray(file.getInputStream());

        return handlingChainingProcessManagerService.startProcess(
                handlingChainingProcessManagerService.createExcelImportDataHandlerChainProcess(ImportDataCommand.builder()
                        .importMetaDataDto(importMetaData)
                        .file(bytes)
                        .orgId(orgId)
                        .build()));
    }

    @Transactional
    public HandlerChainProcessStatus importCsvProductList(final MultipartFile file, final ProductListImportDTO importMetaData, Long orgId) throws Exception {

        final byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        return handlingChainingProcessManagerService.startProcess(
                handlingChainingProcessManagerService.createCsvImportDataHandlerChainProcess(ImportDataCommand.builder()
                        .importMetaDataDto(importMetaData)
                        .file(bytes)
                        .orgId(orgId)
                        .build()));
    }

}




