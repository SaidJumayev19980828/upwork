package com.nasnav.commons.model.handler;


import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.dto.VariantUpdateDTO;
import com.nasnav.service.model.DataImportCachedData;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Data
@Builder
public class ImportDataCommand {

    private byte[] file;

    private ProductListImportDTO importMetaDataDto;

    private Long orgId;

    private Long userId;

    private List<ProductImportDTO> productsData;

    private ProductImportMetadata importMetadata;

    private ImportProductContext context;

    private DataImportCachedData cache;

    private ProductDataLists productsDataLists;

}