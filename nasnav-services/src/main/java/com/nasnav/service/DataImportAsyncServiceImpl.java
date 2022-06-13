package com.nasnav.service;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.service.handler.HandlingChainingProcessManagerService;
import lombok.RequiredArgsConstructor;
import org.apache.tika.io.IOUtils;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

@Service
@Qualifier("excel")
@RequiredArgsConstructor
public class DataImportAsyncServiceImpl {

    private final HandlingChainingProcessManagerService handlingChainingProcessManagerService;

    private final SecurityService security;


    @Transactional
    public HandlerChainProcessStatus importExcelProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData) throws Exception {

        final byte[] bytes = IOUtils.toByteArray(file.getInputStream());

        return handlingChainingProcessManagerService.startProcess(
                handlingChainingProcessManagerService.createExcelImportDataHandlerChainProcess(ImportDataCommand.builder()
                        .importMetaDataDto(importMetaData)
                        .file(bytes)
                        .orgId(security.getCurrentUserOrganizationId())
                        .build()));

    }


    public HandlerChainProcessStatus importCsvProductList(final MultipartFile file, final ProductListImportDTO importMetaData) throws Exception {

        final byte[] bytes = IOUtils.toByteArray(file.getInputStream());
        return handlingChainingProcessManagerService.startProcess(
                handlingChainingProcessManagerService.createCsvImportDataHandlerChainProcess(ImportDataCommand.builder()
                        .importMetaDataDto(importMetaData)
                        .file(bytes)
                        .orgId(security.getCurrentUserOrganizationId())
                        .build()));

    }

}




