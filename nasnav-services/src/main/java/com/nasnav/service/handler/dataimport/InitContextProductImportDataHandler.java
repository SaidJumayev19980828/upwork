package com.nasnav.service.handler.dataimport;

import com.nasnav.commons.model.dataimport.ProductImportDTO;
import com.nasnav.commons.model.handler.HandlerChainProcessStatus;
import com.nasnav.commons.model.handler.ImportDataCommand;
import com.nasnav.dao.BrandsRepository;
import com.nasnav.dao.TagsRepository;
import com.nasnav.dto.BrandDTO;
import com.nasnav.dto.ProductImportMetadata;
import com.nasnav.dto.TagsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.TagsEntity;
import com.nasnav.response.OrganizationResponse;
import com.nasnav.service.CategoryService;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.handler.Handler;
import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import static com.nasnav.constatnts.EntityConstants.Operation.CREATE;
import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toSet;

@Service(HandlerChainFactory.INIT_CONTEXT_PRODUCT_IMPORT_DATA)
@RequiredArgsConstructor
@Slf4j
public class InitContextProductImportDataHandler implements Handler<ImportDataCommand> {

    private final BrandsRepository brandRepo;

    private final OrganizationService organizationService;

    private final TagsRepository tagsRepo;

    private final CategoryService categoryService;

    @Override
    public void handle(final ImportDataCommand importDataCommand, HandlerChainProcessStatus status) throws Exception {

        ImportProductContext context = new ImportProductContext(importDataCommand.getProductsData(), importDataCommand.getImportMetadata());

        final ProductImportMetadata productImportMetadata = importDataCommand.getImportMetadata();
        if (productImportMetadata.isInsertNewProducts() || productImportMetadata.isUpdateProduct()) {
            importNonExistingBrands(context,importDataCommand.getOrgId());
            importNonExistingTags(context, importDataCommand.getOrgId());
        }

        importDataCommand.setContext(context);
    }

    @Override
    public String getName() {

        return HandlerChainFactory.INIT_CONTEXT_PRODUCT_IMPORT_DATA;
    }

    //TODO Check Duplication DataImportServiceImpl
    private void importNonExistingBrands(ImportProductContext context,Long orgId) {

        context
                .getProducts()
                .stream()
                .map(ProductImportDTO::getBrand)
                .distinct()
                .filter(Objects::nonNull)
                .filter(s ->  isBrandNotExists(s,orgId))
                .map(this::toBrandDTO)
                .map(this::createBrand)
                .forEach(brand -> logBrandCreation(brand, context));
    }

    //TODO Check Duplication DataImportServiceImpl
    private BrandDTO createBrand(BrandDTO brandDto) {

        try {
            OrganizationResponse response = organizationService.createOrganizationBrand(brandDto, null, null, null);
            BrandDTO createdBrand = new BrandDTO();
            createdBrand.setId(response.getBrandId());
            createdBrand.setName(brandDto.getName());
            return createdBrand;
        } catch (Throwable t) {
            log.error("Creating Brand ", t);
            throw new RuntimeBusinessException(
                    format("Failed to import brand with name [%s]", brandDto.getName())
                    , "INTEGRATION FAILURE"
                    , HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    //TODO Check Duplication DataImportServiceImpl
    private BrandDTO toBrandDTO(String brandName) {

        BrandDTO dto = new BrandDTO();
        dto.setOperation("CREATE");
        dto.setName(brandName);
        return dto;
    }

    //TODO Check Duplication DataImportServiceImpl
    private boolean isBrandNotExists(String brandName,Long orgId) {

        return !brandRepo.existsByNameIgnoreCaseAndOrganizationEntity_idAndRemoved(brandName, orgId, 0);
    }

    //TODO Check Duplication DataImportServiceImpl
    private void logBrandCreation(BrandDTO brand, ImportProductContext context) {

        context.logNewBrand(brand.getId(), brand.getName());
    }

    //TODO Check Duplication DataImportServiceImpl
    private void importNonExistingTags(ImportProductContext context, final Long orgId) {

        Set<String> tagNames = getTagNames(context);

        Map<String, TagsEntity> existingTags = getExistingTags(tagNames,orgId);

        createNonExistingTags(context, tagNames, existingTags);
    }

    //TODO Check Duplication DataImportServiceImpl
    private void createNonExistingTags(ImportProductContext context, Set<String> tagNames,
                                       Map<String, TagsEntity> existingTags) {

        tagNames
                .stream()
                .filter(Objects::nonNull)
                .filter(tagName -> !existingTags.containsKey(tagName.toLowerCase()))
                .map(this::toTagDTO)
                .map(this::createNewTag)
                .forEach(tag -> logTagCreation(tag, context));
    }

    //TODO Check Duplication DataImportServiceImpl
    private TagsDTO toTagDTO(String tagName) {

        TagsDTO dto = new TagsDTO();
        dto.setOperation(CREATE.getValue());
        dto.setName(tagName);
        dto.setHasCategory(false);
        return dto;
    }

    //TODO Check Duplication DataImportServiceImpl
    private TagsEntity createNewTag(TagsDTO tag) {

        try {
            return categoryService.createOrUpdateTag(tag);
        } catch (BusinessException e) {
            log.error("create New tag", e);
            throw new RuntimeBusinessException(e);
        }
    }

    //TODO Check Duplication DataImportServiceImpl
    private Set<String> getTagNames(ImportProductContext context) {

        return
                Flux
                        .fromIterable(context.getProducts())
                        .flatMapIterable(ProductImportDTO::getTags)
                        .filter(Objects::nonNull)
                        .distinct()
                        .collect(toSet())
                        .block();
    }

    //TODO Check Duplication DataImportServiceImpl
    private Map<String, TagsEntity> getExistingTags(Set<String> tagsNames,Long orgId) {

        Set<String> tagNamesInLowerCase = toLowerCase(tagsNames);
        return
                Flux
                        .fromIterable(tagNamesInLowerCase)
                        .window(500)
                        .map(Flux::buffer)
                        .flatMap(Flux::single)
                        .map(HashSet::new)
                        .flatMapIterable(tagNamesBatch -> findTagsNameLowerCaseInAndOrganizationId(tagNamesBatch, orgId))
                        .collectMap(tag -> tag.getName().toLowerCase(), tag -> tag)
                        .block();
    }

    //TODO Check Duplication DataImportServiceImpl
    private Set<TagsEntity> findTagsNameLowerCaseInAndOrganizationId(Set<String> tagNamesInLowerCase, Long orgId) {

        if (tagNamesInLowerCase == null || tagNamesInLowerCase.isEmpty()) {
            return emptySet();
        }
        return tagsRepo.findByNameLowerCaseInAndOrganizationEntity_Id(tagNamesInLowerCase, orgId);
    }

    //TODO Check Duplication DataImportServiceImpl
    private Set<String> toLowerCase(Set<String> tagsNames) {

        return tagsNames
                .stream()
                .map(String::toLowerCase)
                .collect(toSet());
    }

    //TODO Check Duplication DataImportServiceImpl
    private void logTagCreation(TagsEntity tag, ImportProductContext context) {

        context.logNewTag(tag.getId(), tag.getName());
    }


}
