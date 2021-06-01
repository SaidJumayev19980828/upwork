package com.nasnav.service;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.anyIsTrue;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.*;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.exceptions.ErrorCodes.*;
import static java.util.stream.Collectors.toList;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

public abstract class AbstractCsvExcelDataImportService implements CsvExcelDataImportService {

    @Autowired
    protected ShopsRepository shopRepo;

    @Autowired
    protected SecurityService security;

    @Autowired
    protected ProductFeaturesRepository featureRepo;

    @Autowired
    protected ProductFeaturesRepository productFeaturesRepo;

    @Autowired
    protected DataImportService dataImportService;

    @Autowired
    protected ExtraAttributesRepository extraAttrRepo;

    @Autowired
    protected ImportExportHelper helper;


    private Logger logger = Logger.getLogger(getClass());



    protected ProductImportMetadata getImportMetaData(ProductListImportDTO csvImportMetaData) {
        var importMetadata = new ProductImportMetadata();

        importMetadata.setDryrun(csvImportMetaData.isDryrun());
        importMetadata.setUpdateProduct(csvImportMetaData.isUpdateProduct());
        importMetadata.setUpdateStocks(csvImportMetaData.isUpdateStocks());
        importMetadata.setShopId(csvImportMetaData.getShopId());
        importMetadata.setCurrency(csvImportMetaData.getCurrency());
        importMetadata.setEncoding(csvImportMetaData.getEncoding());
        importMetadata.setDeleteOldProducts(csvImportMetaData.isDeleteOldProducts());
        importMetadata.setResetTags(csvImportMetaData.isResetTags());
        importMetadata.setInsertNewProducts(csvImportMetaData.isInsertNewProducts());

        return importMetadata;
    }



    protected void validateProductImporFile(@Valid MultipartFile file) throws RuntimeBusinessException {
        if(file == null || file.isEmpty()) {
            throw new RuntimeBusinessException(
                    ERR_NO_FILE_UPLOADED
                    , "INVALID PARAM"
                    , NOT_ACCEPTABLE);
        }

    }


    protected void validateProductImportMetaData(@Valid ProductListImportDTO metaData) throws RuntimeBusinessException{
        var shopId = metaData.getShopId();
        var encoding = metaData.getEncoding();
        var currency = metaData.getCurrency();

        if( anyIsNull(shopId, encoding, currency)) {
            throw new RuntimeBusinessException(
                    ERR_PRODUCT_IMPORT_MISSING_PARAM
                    , "MISSING PARAM"
                    , NOT_ACCEPTABLE);
        }

        validateFlags(metaData);
        validateShopId(shopId);
        validateEncodingCharset(encoding);
        validateStockCurrency(currency);
    }



    private void validateFlags(ProductListImportDTO metaData){
        var isStoreManager = security.currentUserHasMaxRoleLevelOf(STORE_MANAGER);
        if(isStoreManager &&
                anyIsTrue(metaData.isUpdateProduct(), metaData.isDeleteOldProducts()
                        , metaData.isInsertNewProducts(), metaData.isResetTags())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMPORT$0001);
        }
    };


    private void validateEncodingCharset(String encoding) throws RuntimeBusinessException {
        try {
            if( !Charset.isSupported(encoding)) {
                throw new IllegalStateException();
            }
        }catch(Exception e) {
            logger.error(e,e);
            throw new RuntimeBusinessException(
                    String.format(ERR_INVALID_ENCODING, encoding)
                    , "MISSING PARAM:encoding"
                    , NOT_ACCEPTABLE);
        }
    }





    private void validateShopId(Long shopId) throws RuntimeBusinessException {
        var shop =
                shopRepo
                .findById(shopId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_ACCEPTABLE, S$0002, shopId));

        validateShopBelongsToOrganization(shop);
        helper.validateAdminCanManageTheShop(shopId);
    }






    private void validateShopBelongsToOrganization(ShopsEntity shop) {
        var userOrgId = security.getCurrentUserOrganizationId();
        var shopOrgId = shop.getOrganizationEntity().getId();

        if(!Objects.equals(shopOrgId, userOrgId)) {
            throw new RuntimeBusinessException(
                    String.format(ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP, userOrgId, shopOrgId)
                    , "MISSING PARAM:shop_id"
                    , NOT_ACCEPTABLE);
        }
    }


    private void validateStockCurrency(Integer currency) throws RuntimeBusinessException {
        var invalidCurrency = Arrays.asList( TransactionCurrency.values() )
                .stream()
                .map(TransactionCurrency::getValue)
                .map(Integer::valueOf)
                .noneMatch(val -> Objects.equals(currency, val));
        if(invalidCurrency ) {
            throw new RuntimeBusinessException(
                    String.format("Invalid Currency code [%d]!", currency)
                    , "INVALID_PARAM:currency"
                    , NOT_ACCEPTABLE);
        }
    }



    @Override
    public List<String> getProductImportTemplateHeaders() {
        var orgId = security.getCurrentUserOrganizationId();
        var features =
                productFeaturesRepo
                        .findByOrganizationId(orgId)
                        .stream()
                        .map(ProductFeaturesEntity::getName)
                        .sorted()
                        .collect(toList());

        var extraAttributes =
                extraAttrRepo.findByOrganizationId(orgId)
                        .stream()
                        .map(ExtraAttributesEntity::getName)
                        .sorted()
                        .collect(toList());

        List<String> baseHeaders = new ArrayList<>(CSV_BASE_HEADERS);
        baseHeaders.addAll(features);
        baseHeaders.addAll(extraAttributes);
        return baseHeaders;
    }

    @Override
    public ByteArrayOutputStream generateProductsTemplate() throws IOException {
        var baseHeaders = getProductImportTemplateHeaders();

        return writeFileHeaders(baseHeaders);
    }

    abstract ByteArrayOutputStream writeFileHeaders(List<String> headers) throws IOException;

}
