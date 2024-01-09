package com.nasnav.service;

import com.google.gson.Gson;
import com.nasnav.dao.ProductImagesRepository;
import com.nasnav.dao.ProductImgsCustomRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.dto.ProductImageDTO;
import com.nasnav.dto.VariantWithNoImagesDTO;
import com.nasnav.enumerations.ImageFileTemplateType;
import com.nasnav.request.OrderSearchParam;
import com.nasnav.response.OrdersListResponse;
import com.nasnav.service.model.importproduct.csv.CsvRow;
import com.nasnav.service.model.importproduct.csv.OrderRow;
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
import static com.nasnav.service.CsvExcelDataImportService.ORDER_DATA_COLUMN;
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
    @Autowired
    protected OrderService orderService;

    protected Boolean addExcelDataValidation;

    @Override
    public ByteArrayOutputStream generateProductsFile(Long shopId, Boolean addExcelDataValidation) throws IOException {
        Long orgId = security.getCurrentUserOrganizationId();
        this.addExcelDataValidation = addExcelDataValidation;
        List<String> headers = importService.getProductImportTemplateHeaders();
        List<CsvRow> products = exportService.exportProductsData(orgId, shopId);
        return buildProductsFile(headers, products);
    }

    @Override
    public ByteArrayOutputStream generateOrdersFile(OrderSearchParam params) throws IOException {
        this.addExcelDataValidation = false;
        OrdersListResponse orders = exportService.exportOrdersData(params);
        System.out.println("orders length" + orders.getTotal());
        Gson gson = new Gson();
        System.out.println(gson.toJson(orders.getOrders()));
        System.out.println("generateOrdersFile");
        return buildOrdersFile(ORDER_DATA_COLUMN,orders.getOrders());

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

    protected abstract ByteArrayOutputStream buildOrdersFile(List<String> headers, List<DetailedOrderRepObject> orders) throws IOException;

    protected abstract ByteArrayOutputStream buildProductsFile(List<String> headers, List<CsvRow> products) throws IOException;

    protected abstract ByteArrayOutputStream buildImagesFile(List<String> headers, List<ProductImageDTO> images) throws IOException;

    protected void removeSpecialColumns(Map<String, String> imgDataToColumnMapping) {
        imgDataToColumnMapping.remove("externalId");
        imgDataToColumnMapping.remove("productName");
        imgDataToColumnMapping.remove("externalId");
        imgDataToColumnMapping.put("imagePath", "image_path");
    }

    protected ByteArrayOutputStream generateEmptyImagesFileTemplate() throws IOException {
        return writeFileHeaders(IMG_CSV_BASE_HEADERS);
    }

    protected abstract ByteArrayOutputStream writeFileHeaders(List<String> headers) throws IOException ;

    protected abstract ByteArrayOutputStream buildProductWithNoImgsFile(List<String> headers, List<VariantWithNoImagesDTO> variants) throws IOException;



}
