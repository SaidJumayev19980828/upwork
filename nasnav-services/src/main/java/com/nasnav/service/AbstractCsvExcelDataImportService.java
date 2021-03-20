package com.nasnav.service;

import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.ExtraAttributesRepository;
import com.nasnav.dao.ProductFeaturesRepository;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.ExtraAttributesEntity;
import com.nasnav.persistence.ProductFeaturesEntity;
import com.nasnav.persistence.ShopsEntity;
import com.univocity.parsers.csv.CsvWriter;
import com.univocity.parsers.csv.CsvWriterSettings;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.multipart.MultipartFile;
import javax.validation.Valid;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.*;
import static java.util.stream.Collectors.toList;

public abstract class AbstractCsvExcelDataImportService implements CsvExcelDataImportService {

    @Autowired
    protected ShopsRepository shopRepo;

    @Autowired
    protected EmployeeUserRepository empRepo;

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


    private Logger logger = Logger.getLogger(getClass());



    protected ProductImportMetadata getImportMetaData(ProductListImportDTO csvImportMetaData) {
        ProductImportMetadata importMetadata = new ProductImportMetadata();

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
                    , HttpStatus.NOT_ACCEPTABLE);
        }

    }


    protected void validateProductImportMetaData(@Valid ProductListImportDTO metaData) throws RuntimeBusinessException{
        Long shopId = metaData.getShopId();
        String encoding = metaData.getEncoding();
        Integer currency = metaData.getCurrency();

        if( anyIsNull(shopId, encoding, currency)) {
            throw new RuntimeBusinessException(
                    ERR_PRODUCT_IMPORT_MISSING_PARAM
                    , "MISSING PARAM"
                    , HttpStatus.NOT_ACCEPTABLE);
        }

        validateShopId(shopId);

        validateEncodingCharset(encoding);

        validateStockCurrency(currency);
    }





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
                    , HttpStatus.NOT_ACCEPTABLE);
        }
    }





    private void validateShopId(Long shopId) throws RuntimeBusinessException {
        if( !shopRepo.existsById( shopId ) ) {
            throw new RuntimeBusinessException(
                    String.format(ERR_SHOP_ID_NOT_EXIST, shopId)
                    , "MISSING PARAM:shop_id"
                    , HttpStatus.NOT_ACCEPTABLE);
        }


        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        EmployeeUserEntity user =  empRepo.getOneByEmail(auth.getName());
        Long userOrgId = user.getOrganizationId();

        ShopsEntity shop = shopRepo.findById(shopId).get();
        Long shopOrgId = shop.getOrganizationEntity().getId();

        if(!Objects.equals(shopOrgId, userOrgId)) {
            throw new RuntimeBusinessException(
                    String.format(ERR_USER_CANNOT_CHANGE_OTHER_ORG_SHOP, userOrgId, shopOrgId)
                    , "MISSING PARAM:shop_id"
                    , HttpStatus.NOT_ACCEPTABLE);
        }
    }





    private void validateStockCurrency(Integer currency) throws RuntimeBusinessException {
        boolean invalidCurrency = Arrays.asList( TransactionCurrency.values() )
                .stream()
                .map(TransactionCurrency::getValue)
                .map(Integer::valueOf)
                .noneMatch(val -> Objects.equals(currency, val));
        if(invalidCurrency ) {
            throw new RuntimeBusinessException(
                    String.format("Invalid Currency code [%d]!", currency)
                    , "INVALID_PARAM:currency"
                    , HttpStatus.NOT_ACCEPTABLE);
        }
    }



    @Override
    public List<String> getProductImportTemplateHeaders() {
        Long orgId = security.getCurrentUserOrganizationId();
        List<String> features =
                productFeaturesRepo
                        .findByOrganizationId(orgId)
                        .stream()
                        .map(ProductFeaturesEntity::getName)
                        .sorted()
                        .collect(toList());

        List<String> extraAttributes =
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
    public ByteArrayOutputStream generateProductsCsvTemplate() throws IOException {
        List<String> baseHeaders = getProductImportTemplateHeaders();

        return writeCsvHeaders(baseHeaders);
    }

    private ByteArrayOutputStream writeCsvHeaders(List<String> headers) throws IOException {
        ByteArrayOutputStream csvResult = new ByteArrayOutputStream();
        Writer outputWriter = new OutputStreamWriter(csvResult);

        CsvWriter writer = new CsvWriter(outputWriter, createWritingSettings());

        writer.writeHeaders(headers);
        writer.close();
        csvResult.close();

        return csvResult;
    }

    private CsvWriterSettings createWritingSettings() {
        CsvWriterSettings settings = new CsvWriterSettings();
        return settings;
    }
}
