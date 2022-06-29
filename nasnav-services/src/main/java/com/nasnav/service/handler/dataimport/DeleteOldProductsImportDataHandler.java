package com.nasnav.service.handler.dataimport;


import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dao.ProductRepository;
import com.nasnav.service.ProductServiceTransactions;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import com.nasnav.service.model.importproduct.context.Product;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toSet;

@Service(HandlerChainFactory.DELETE_OLD_PRODUCT_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class DeleteOldProductsImportDataHandler implements Handler<ImportDataCommand> {

    private final ProductServiceTransactions productServiceTransactions;

    private final ProductRepository productRepo;

    @Override
    public void handle(final ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {

        if (importDataCommand.getImportMetadata().isDeleteOldProducts()) {
            Set<Long> productsToDelete = getProductsToDelete(importDataCommand.getContext(),importDataCommand.getOrgId());
            productServiceTransactions.deleteProducts(new ArrayList<>(productsToDelete), true);
        }
    }

    @Override
    public String getName() {

        return HandlerChainFactory.DELETE_OLD_PRODUCT_IMPORT_DATA;
    }

    //TODO Check Duplication DataImportServiceImpl
    private Set<Long> getProductsToDelete(ImportProductContext context,Long orgId) {

        Set<Long> productsToDelete = productRepo.listProductIdByOrganizationId(orgId);
        Set<Long> processedProducts = getProcessedProducts(context);
        processedProducts.forEach(productsToDelete::remove);
        return productsToDelete;
    }

    //TODO Check Duplication DataImportServiceImpl
    private Set<Long> getProcessedProducts(ImportProductContext context) {

        Set<Long> processedProducts =
                Stream.concat(context.getCreatedProducts().stream(), context.getUpdatedProducts().stream())
                        .map(Product::getId)
                        .collect(toSet());
        if (processedProducts.isEmpty()) {
            processedProducts.add(-1L);
        }
        return processedProducts;
    }

}
