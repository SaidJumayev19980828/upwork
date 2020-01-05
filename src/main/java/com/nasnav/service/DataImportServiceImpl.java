package com.nasnav.service;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.constatnts.EntityConstants;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.CategoriesRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ProductVariantsRepository;
import com.nasnav.dto.*;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.integration.IntegrationService;
import com.nasnav.integration.enums.MappingType;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.persistence.ProductVariantsEntity;
import com.nasnav.response.ProductListImportResponse;
import com.nasnav.response.ProductUpdateResponse;
import com.nasnav.response.VariantUpdateResponse;
import lombok.Data;
import org.apache.commons.beanutils.BeanUtils;
import org.jboss.logging.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static com.nasnav.constatnts.error.dataimport.ErrorMessages.*;

@Service
public class DataImportServiceImpl implements DataImportService {

    @Autowired
    private CategoriesRepository categoriesRepo;

    @Autowired
    private BrandsRepository brandRepo;

    @Autowired
    private ProductVariantsRepository variantRepo;

    @Autowired
    private ProductService productService;

    @Autowired
    private StockService stockService;

    @Autowired
    private ProductRepository productRepo;

    @Autowired
    private SecurityService security;

    @Autowired
    private IntegrationService integrationService;

    private Logger logger = Logger.getLogger(getClass());

    @Override
    public ProductListImportResponse importProducts(List<ProductImportDTO> productImportDTOS, ProductImportMetadata productImportMetadata) throws BusinessException {

        List<ProductImportData> importedDtos = toProductImportDto(productImportDTOS, productImportMetadata);

        saveToDB(importedDtos, productImportMetadata);

        if(productImportMetadata.isDryrun())
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();

        return new ProductListImportResponse(Collections.emptyList());
    }

    private void saveToDB(List<ProductImportData> importedDtos, ProductImportMetadata importMetaData) throws BusinessException {
        List<String> errors = new ArrayList<>();

        for (int i = 0; i < importedDtos.size(); i++) {
            ProductImportData dto = importedDtos.get(i);
            try {
                saveSingleProductCsvRowToDB(dto, importMetaData);
            } catch (Exception e) {
                logger.error(e, e);

                StringBuilder msg = new StringBuilder();
                msg.append(String.format("Error at Row[%d], with data[%s]", i + 1, dto.toString()));
                msg.append(System.getProperty("line.separator"));
                msg.append("Error Message: " + e.getMessage());

                errors.add(msg.toString());
            }
        }

        if (!errors.isEmpty()) {
            JSONArray json = new JSONArray(errors);
            throw new BusinessException(
            		ERR_PRODUCT_DB_SAVE
                    , json.toString()
                    , HttpStatus.NOT_ACCEPTABLE);
        }

    }


    private void saveSingleProductCsvRowToDB(ProductImportData dto, ProductImportMetadata importMetaData) throws BusinessException {
        if (dto.isExisting()) {
            if (importMetaData.isUpdateProduct()) {
                saveProductDto(dto.getProductDto());
                VariantUpdateResponse variantResponse = productService.updateVariant(dto.getVariantDto());
                saveExternalMapping(dto, variantResponse.getVariantId());
            }

            if (importMetaData.isUpdateStocks()) {
                stockService.updateStock(dto.getStockDto());
            }
        } else {
            saveNewImportedProduct(dto);
        }

    }


    private void saveNewImportedProduct(ProductImportData dto) throws BusinessException {
        Long productId = saveProductDto(dto.getProductDto());
        dto.getVariantDto().setProductId(productId);
        VariantUpdateResponse variantResponse = productService.updateVariant(dto.getVariantDto());
        Long variantId = variantResponse.getVariantId();

        dto.getStockDto().setVariantId(variantId);
        stockService.updateStock(dto.getStockDto());
        saveExternalMapping(dto, variantId);
    }

    private void saveExternalMapping(ProductImportData dto, Long variantId) throws BusinessException{
        if (dto.getExternalId() != null)
            integrationService.addMappedValue(security.getCurrentUserOrganizationId(), MappingType.PRODUCT_VARIANT, variantId.toString(), dto.getExternalId());
    }

    private Long saveProductDto(ProductUpdateDTO dto) throws BusinessException {
        String productDtoJson = getProductDtoJson(dto);
        ProductUpdateResponse productResponse = productService.updateProduct(productDtoJson, false);
        Long productId = productResponse.getProductId();
        return productId;
    }


    private String getProductDtoJson(ProductUpdateDTO dto) throws BusinessException {
        ProductUpdateDTO dtoClone = prepareProductUpdateDto(dto);
        String productDtoJson = "";
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
            productDtoJson = mapper.writeValueAsString(dtoClone);
        } catch (Exception e) {
            logger.error(e, e);
            throw new BusinessException(
                    String.format(ERR_CONVERT_TO_JSON, dtoClone.getClass().getName())
                    , "INTERNAL SERVER ERROR"
                    , HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return productDtoJson;
    }


    private ProductUpdateDTO prepareProductUpdateDto(ProductUpdateDTO dto) throws BusinessException {
        try {
            ProductUpdateDTO dtoClone = (ProductUpdateDTO) BeanUtils.cloneBean(dto);
            Optional<ProductEntity> product = productRepo.findByName(dto.getName());
            if (product.isPresent()) {
                dtoClone.setId(product.get().getId());
                dtoClone.setOperation(EntityConstants.Operation.UPDATE);
            }
            return dtoClone;
        } catch (Exception e) {
            logger.error(e, e);
            throw new BusinessException(
                    String.format(ERR_PREPARE_PRODUCT_DTO_DATA, dto.toString())
                    , "INTERNAL SERVER ERROR"
                    , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }


    private List<ProductImportData> toProductImportDto(List<ProductImportDTO> rows, ProductImportMetadata importMetaData) throws BusinessException {
        List<ProductImportData> dtoList = new ArrayList<>();
        for (ProductImportDTO row : rows) {
            dtoList.add(toProductImportDto(row, importMetaData));
        }

        return dtoList;
    }


    private ProductImportData toProductImportDto(ProductImportDTO row, ProductImportMetadata importMetaData) throws BusinessException {
        ProductImportData dto = new ProductImportData();

        dto.setOriginalRowData(row.toString());
        dto.setProductDto(createProductDto(row));
        dto.setVariantDto(createVariantDto(row));
        dto.setStockDto(createStockDto(row, importMetaData));
        dto.setExternalId(row.getExternalId());

        Long orgId = security.getCurrentUserOrganizationId();

        Optional<ProductVariantsEntity> variantEnt = null;

        if (isNotBlankOrNull( dto.getVariantDto().getVariantId() )) {
            variantEnt = variantRepo.findByIdAndProductEntity_OrganizationId(dto.getVariantDto().getVariantId(), orgId);
            if (!variantEnt.isPresent())
                throw new BusinessException("No variant found with id " + dto.getVariantDto().getVariantId(),
                        "INVALID PARAM: variant_id",HttpStatus.NOT_ACCEPTABLE);
        }

        if (variantEnt == null && isNotBlankOrNull(row.getExternalId()) ) {
            String localMappingId = integrationService.getLocalMappedValue(orgId, MappingType.PRODUCT_VARIANT, row.getExternalId());
            if(localMappingId != null && StringUtils.validateUrl(localMappingId, "[0-9]+"))
                variantEnt = variantRepo.findByIdAndProductEntity_OrganizationId(Long.parseLong(localMappingId), orgId);
        }

        if (variantEnt == null && isNotBlankOrNull(row.getBarcode()))
            variantEnt = variantRepo.findByBarcodeAndProductEntity_OrganizationId(row.getBarcode(), orgId);

        if (variantEnt != null && variantEnt.isPresent()) {
            modifyProductCsvImportDtoForUpdate(dto, row, variantEnt.get());
        }

        return dto;
    }


    private void modifyProductCsvImportDtoForUpdate(ProductImportData dto, ProductImportDTO row, ProductVariantsEntity variantEnt)
            throws BusinessException {
        dto.setExisting(true);

        ProductUpdateDTO product = dto.getProductDto();
        VariantUpdateDTO variant = dto.getVariantDto();
        StockUpdateDTO stock = dto.getStockDto();

        Long productId = variantEnt.getProductEntity().getId();
        variant.setProductId(productId);
        variant.setVariantId(variantEnt.getId());
        variant.setOperation(EntityConstants.Operation.UPDATE);

        product.setOperation(EntityConstants.Operation.UPDATE);
        product.setId(productId);

        stock.setVariantId(variantEnt.getId());
    }


    private StockUpdateDTO createStockDto(ProductImportDTO row, ProductImportMetadata importMetaData) {
        StockUpdateDTO stock = new StockUpdateDTO();
        stock.setCurrency(importMetaData.getCurrency());
        stock.setShopId(importMetaData.getShopId());
        stock.setPrice(row.getPrice());
        stock.setQuantity(row.getQuantity());
        return stock;
    }


    private VariantUpdateDTO createVariantDto(ProductImportDTO row) {
        String features = Optional.ofNullable(row.getFeatures())
                .map(JSONObject::new)
                .map(JSONObject::toString)
                .orElse(null);

        VariantUpdateDTO variant = new VariantUpdateDTO();
        variant.setVariantId(row.getVariantId());
        variant.setBarcode(row.getBarcode());
        variant.setFeatures("{}");
        variant.setDescription(row.getDescription());
        variant.setName(row.getName());
        variant.setOperation(EntityConstants.Operation.CREATE);
        variant.setPname(row.getPname());
        if (features != null) {
            variant.setFeatures(features);
        }

        return variant;
    }


    private ProductUpdateDTO createProductDto(ProductImportDTO row) throws BusinessException {
        Long categoryId = categoriesRepo.findByName(row.getCategory());
        if (categoryId == null) {
            throw new BusinessException(
                    String.format(ERR_CATEGORY_NAME_NOT_EXIST, row.getCategory())
                    , "INVALID DATA:category"
                    , HttpStatus.NOT_ACCEPTABLE);
        }

        Long brandId = brandRepo.findByName(row.getBrand());
        if (brandId == null) {
            throw new BusinessException(
                    String.format(ERR_BRAND_NAME_NOT_EXIST, row.getBrand())
                    , "INVALID DATA:brand"
                    , HttpStatus.NOT_ACCEPTABLE);
        }


        ProductUpdateDTO product = new ProductUpdateDTO();
        product.setBrandId(brandId);
        product.setCategoryId(categoryId);
        product.setDescription(row.getDescription());
        product.setBarcode(row.getBarcode());
        product.setName(row.getName());
        product.setOperation(EntityConstants.Operation.CREATE);
        product.setPname(row.getPname());
        return product;
    }
}


@Data
class ProductImportData{
    private VariantUpdateDTO variantDto;
    private ProductUpdateDTO productDto;
    private StockUpdateDTO stockDto;
    private boolean existing;
    private String originalRowData;
    private String externalId;

    public ProductImportData() {
        variantDto = new VariantUpdateDTO();
        productDto = new ProductUpdateDTO();
        stockDto = new StockUpdateDTO();
        existing = false;
        originalRowData = "[]";
    }
}