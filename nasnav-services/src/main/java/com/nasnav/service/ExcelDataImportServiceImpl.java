package com.nasnav.service;

import static java.util.Collections.emptyList;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

import javax.validation.Valid;

import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.jboss.logging.Logger;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.csv.CsvRow;

@Service
public class ExcelDataImportServiceImpl extends AbstractCsvExcelDataImportService {

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public ImportProductContext importProductList(@Valid MultipartFile file, @Valid ProductListImportDTO importMetaData) throws BusinessException, ImportProductException {
        validateProductImportMetaData(importMetaData);
        validateProductImportCsvFile(file);

        ProductImportMetadata importMetadata = getImportMetaData(importMetaData);
        ImportProductContext initialContext = new ImportProductContext(emptyList(), importMetadata);

        List<CsvRow> rows = parseExcelFile(file, importMetaData, initialContext);

        return initialContext;

    }

    private List<CsvRow> parseExcelFile(MultipartFile file, ProductListImportDTO importMetaData, ImportProductContext initialContext) throws ImportProductException {
        List<ProductFeaturesEntity> orgFeatures = featureRepo.findByShopId( importMetaData.getShopId() );
        try {
            HSSFWorkbook wb = new HSSFWorkbook(file.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
            throw  new ImportProductException(e, initialContext);
        }
        return null;
    }

    @Override
    public ByteArrayOutputStream generateProductsCsvTemplate(){
        List<String> baseHeaders = getProductImportTemplateHeaders();
        return null;
    }
}
