package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.IndexedData;
import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.exceptions.ImportProductException;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.model.DataImportCachedData;
import com.nasnav.service.model.VariantCache;
import com.nasnav.service.model.VariantIdentifier;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.IntStream;

import static com.nasnav.commons.utils.EntityUtils.allIsNull;
import static java.lang.String.format;
import static java.util.Objects.nonNull;
import static java.util.Optional.ofNullable;


@Service(HandlerChainFactory.VALIDATE_PRODUCT_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class ValidateProductDataImportDataHandler implements Handler<ImportDataCommand> {

    @Override
    public void handle(final ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {
        validateProductData(importDataCommand.getProductsData(), importDataCommand.getCache(), importDataCommand.getContext());
    }

    @Override
    public String getName() {

        return HandlerChainFactory.VALIDATE_PRODUCT_IMPORT_DATA;
    }


    //TODO Check Duplication DataImportServiceImpl
    private void validateProductData(List<ProductImportDTO> productImportDTOS, DataImportCachedData cache,
                                     ImportProductContext context) throws ImportProductException {

        IntStream
                .range(0, productImportDTOS.size())
                .mapToObj(i -> new IndexedData<>(i, productImportDTOS.get(i)))
                .map(product -> new IndexedData<>(product.getIndex(), toVariantIdentifier(product.getData())))
                .filter(variantId -> !isNullVariantIdentifier(variantId.getData()))
                .filter(variantId -> isNoVariantExistWithId(variantId.getData(), cache.getVariantsCache()))
                .map(this::createErrorMessage)
                .forEach(err -> context.logNewError(err.getData(), err.getIndex()+1));

        if(!context.getErrors().isEmpty()) {
            throw new ImportProductException(context);
        }
    }

    //TODO Check Duplication DataImportServiceImpl
    private VariantIdentifier toVariantIdentifier(ProductImportDTO row) {
        VariantIdentifier identifier = new VariantIdentifier();
        String variantId = ofNullable(row.getVariantId()).map(String::valueOf).orElse(null);
        identifier.setVariantId(variantId);
        identifier.setExternalId(row.getExternalId());
        identifier.setBarcode(row.getBarcode());
        return identifier;
    }

    //TODO Check Duplication DataImportServiceImpl
    private boolean isNullVariantIdentifier(VariantIdentifier identifiers) {
        return nonNull(identifiers) && allIsNull(identifiers.getVariantId());
    }

    //TODO Check Duplication DataImportServiceImpl
    private boolean isNoVariantExistWithId(VariantIdentifier identifier, VariantCache cache) {
        return !cache.getIdToVariantMap().containsKey(identifier.getVariantId());
    }

    //TODO Check Duplication DataImportServiceImpl
    private IndexedData<String> createErrorMessage(IndexedData<VariantIdentifier> variantId) {
        return new IndexedData<>(variantId.getIndex()
                ,format("No variant found with id[%s] nor external Id[%s] at row[%d]!"
                , variantId.getData().getVariantId()
                , variantId.getData().getExternalId()
                , variantId.getIndex() + 1));
    }
}
