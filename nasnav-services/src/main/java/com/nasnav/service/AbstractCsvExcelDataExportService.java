package com.nasnav.service;

import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductImgsCustomRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.VariantWithNoImagesDTO;
import com.nasnav.enumerations.ImageFileTemplateType;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.nasnav.commons.utils.CollectionUtils.mapInBatches;
import static com.nasnav.enumerations.ImageFileTemplateType.EMPTY;
import static com.nasnav.enumerations.ImageFileTemplateType.PRODUCTS_WITH_NO_IMGS;
import static com.nasnav.service.CsvExcelDataImportService.IMG_CSV_BASE_HEADERS;
import static java.util.Optional.ofNullable;
import static java.util.stream.Collectors.toList;

public abstract class AbstractCsvExcelDataExportService implements CsvExcelDataExportService {
    @Autowired
    protected SecurityService security;

    @Autowired
    protected DataExportService exportService;

    @Autowired
    @Qualifier("csv")
    protected CsvExcelDataImportService importService;

    @Autowired
    protected ProductImgsCustomRepository productImgsCustomRepo;

    @Autowired
    protected ProductImagesRepository productImagesRepo;

    @Autowired
    protected ProductRepository productRepo;


    @Override
    public ByteArrayOutputStream generateProductsFile(Long shopId) throws IOException {
        Long orgId = security.getCurrentUserOrganizationId();

        List<String> headers = importService.getProductImportTemplateHeaders();

        List<CsvRow> products = exportService.exportProductsData(orgId, shopId);

        return buildProductsFile(headers, products);
    }


    @Override
    public ByteArrayOutputStream generateImagesTemplate(ImageFileTemplateType type) throws IOException{
        ImageFileTemplateType templateType = ofNullable(type).orElse(EMPTY);
        if(templateType.equals(PRODUCTS_WITH_NO_IMGS)) {
            return generateImagesFileTemplateForProductsWithNoImgs();
        }
        else {
            return generateEmptyImagesFileTemplate();
        }
    }


    @Override
    public ByteArrayOutputStream generateProductsImagesFile() throws IOException {
        Long orgId = security.getCurrentUserOrganizationId();

        List<String> headers = Arrays.asList("product_id", "variant_id", "barcode", "image_path");

        List<Long> productIdsList = productRepo.findProductsIdsByOrganizationId(orgId);

        List<ProductImageDTO> images =  mapInBatches(productIdsList, 500, productImagesRepo::findByProductsIds)
                .stream()
                .map(i -> (ProductImageDTO) i.getRepresentation())
                .collect(toList());

        return buildImagesFile(headers, images);
    }


    private ByteArrayOutputStream generateImagesFileTemplateForProductsWithNoImgs() throws IOException {
        List<String> headers = new ArrayList<>();
        headers.addAll(IMG_CSV_BASE_HEADERS);
        headers.add("product_name");
        headers.add("product_id");

        Long orgId = security.getCurrentUserOrganizationId();
        List<VariantWithNoImagesDTO> variants = productImgsCustomRepo.getProductsWithNoImages(orgId);

        return buildProductWithNoImgsFile(headers, variants);
    }


    abstract ByteArrayOutputStream buildProductsFile(List<String> headers, List<CsvRow> products) throws IOException;

    abstract ByteArrayOutputStream buildImagesFile(List<String> headers, List<ProductImageDTO> images) throws IOException;

    protected void removeSpecialColumns(Map<String, String> imgDataToColumnMapping) {
        imgDataToColumnMapping.remove("externalId");
        imgDataToColumnMapping.remove("productName");
        imgDataToColumnMapping.remove("externalId");
        imgDataToColumnMapping.put("imagePath", "image_path");
    }

    protected ByteArrayOutputStream generateEmptyImagesFileTemplate() throws IOException {
        return writeFileHeaders(IMG_CSV_BASE_HEADERS);
    }

    abstract ByteArrayOutputStream writeFileHeaders(List<String> headers) throws IOException ;

    abstract ByteArrayOutputStream buildProductWithNoImgsFile(List<String> headers, List<VariantWithNoImagesDTO> variants) throws IOException;
}
