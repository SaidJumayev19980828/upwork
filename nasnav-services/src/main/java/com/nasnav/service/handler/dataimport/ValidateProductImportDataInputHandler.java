package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dao.ShopsRepository;
import com.nasnav.dto.ProductListImportDTO;
import com.nasnav.enumerations.TransactionCurrency;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.service.ImportExportHelper;
import com.nasnav.service.SecurityService;
import com.nasnav.service.handler.Handler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;

import java.nio.charset.Charset;
import java.util.*;
import java.util.stream.Collectors;

import static com.nasnav.commons.utils.EntityUtils.anyIsNull;
import static com.nasnav.commons.utils.EntityUtils.anyIsTrue;
import static com.nasnav.constatnts.error.dataimport.ErrorMessages.*;
import static com.nasnav.enumerations.Roles.STORE_MANAGER;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

@Service(HandlerChainFactory.VALIDATE_PRODUCT_IMPORT_DATA_INPUT)
@RequiredArgsConstructor
@Slf4j
public class ValidateProductImportDataInputHandler implements Handler<ImportDataCommand> {

    private final SecurityService security;
    private final ShopsRepository shopRepo;
    private final ImportExportHelper helper;

    public void handle(ImportDataCommand importDataCommand, HandlerChainProcessStatus status){

        validateProductImportMetaData(importDataCommand.getImportMetaDataDto());
        validateProductImportFile(importDataCommand.getFile());
    }

    @Override
    public String getName() {

        return HandlerChainFactory.VALIDATE_PRODUCT_IMPORT_DATA_INPUT;
    }

    //TODO Check Duplication AbstractCsvExcelDataImportService
    private void validateProductImportMetaData(@Valid ProductListImportDTO metaData) throws RuntimeBusinessException {
        var shopIds = metaData.getShopIds();
        var encoding = metaData.getEncoding();
        var currency = metaData.getCurrency();

        if( anyIsNull(shopIds, encoding, currency)) {
            throw new RuntimeBusinessException(
                    ERR_PRODUCT_IMPORT_MISSING_PARAM
                    , "MISSING PARAM"
                    , NOT_ACCEPTABLE);
        }

        validateFlags(metaData);
        validateAndProcessShops(shopIds);
        validateEncodingCharset(encoding);
        validateStockCurrency(currency);
    }

    //TODO Check Duplication
    private void validateProductImportFile(@Valid byte[] file) throws RuntimeBusinessException {
        if(file == null || ObjectUtils.isEmpty(file)) {
            throw new RuntimeBusinessException(
                    ERR_NO_FILE_UPLOADED
                    , "INVALID PARAM"
                    , NOT_ACCEPTABLE);
        }

    }

    //TODO Check Duplication
    private void validateFlags(ProductListImportDTO metaData){
        var isStoreManager = security.currentUserHasMaxRoleLevelOf(STORE_MANAGER);
        if(isStoreManager &&
                anyIsTrue(metaData.isUpdateProduct(), metaData.isDeleteOldProducts()
                        , metaData.isInsertNewProducts(), metaData.isResetTags())){
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, P$IMPORT$0001);
        }
    }

    //TODO Check Duplication
    public void validateAndProcessShops(List<Long> shopIds) {
        if (shopIds.isEmpty()) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0006);
        }

        List<ShopsEntity> shops = shopRepo.findByIdIn(shopIds);

        Map<Long, ShopsEntity> validShopsMap = shops.stream()
                .filter(this::validateShop)
                .collect(Collectors.toMap(ShopsEntity::getId, shop -> shop));

        shopIds.forEach(id -> {
            if (!validShopsMap.containsKey(id)) {
                throw new RuntimeBusinessException(NOT_ACCEPTABLE, S$0002, id);
            }
        });
    }

    private boolean validateShop(ShopsEntity shop) {
        validateShopBelongsToOrganization(shop);
        helper.validateAdminCanManageTheShop(shop.getId());
        return true;
    }

    //TODO Check Duplication
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

    //TODO Check Duplication
    private void validateEncodingCharset(String encoding) throws RuntimeBusinessException {
        try {
            if( !Charset.isSupported(encoding)) {
                throw new IllegalStateException();
            }
        }catch(Exception e) {
            log.error("Validate encoding charset ",e);
            throw new RuntimeBusinessException(
                    String.format(ERR_INVALID_ENCODING, encoding)
                    , "MISSING PARAM:encoding"
                    , NOT_ACCEPTABLE);
        }
    }

    //TODO Check Duplication
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

}
