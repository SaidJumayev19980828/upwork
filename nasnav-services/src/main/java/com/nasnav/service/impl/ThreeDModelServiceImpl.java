package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.criteria.AbstractCriteriaQueryBuilder;
import com.nasnav.commons.criteria.data.CrieteriaQueryResults;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ThreeDModelRepository;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.request.product.ThreeDModelDTO;
import com.nasnav.dto.response.ThreeDModelList;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
import com.nasnav.request.ThreeDModelSearchParam;
import com.nasnav.service.FileService;
import com.nasnav.service.RoleService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.ThreeDModelService;
import com.nasnav.service.subscription.SubscriptionService;
import lombok.Builder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.nasnav.dto.response.ThreeDModelResponse.get3dModelResponse;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.*;

@Service
@Slf4j
@Builder
public class ThreeDModelServiceImpl implements ThreeDModelService {

    private final String organizationName;
    private final ThreeDModelRepository threeDModelRepository;
    private final OrganizationRepository organizationRepository;
    private final SecurityService securityService;
    private final RoleService roleService;
    private final EmployeeUserRepository employeeUserRepository;
    private final ProductRepository productRepository;
    private final ObjectMapper mapper;
    private final FileService fileService;
    private final SubscriptionService subscriptionService;
    @Qualifier("threeDModelCriteriaQueryBuilder")
    private AbstractCriteriaQueryBuilder<ProductThreeDModel, ThreeDModelSearchParam> criteriaQueryBuilder;

    @Autowired
    public ThreeDModelServiceImpl(@Value("${organization_meetus_ar_name}") String organizationName,
                                  ThreeDModelRepository threeDModelRepository,
                                  OrganizationRepository organizationRepository, SecurityService securityService,
                                  RoleService roleService, EmployeeUserRepository employeeUserRepository,
                                  ProductRepository productRepository, ObjectMapper mapper, FileService fileService,
                                  @Lazy @Qualifier("wert") SubscriptionService subscriptionService,
                                  AbstractCriteriaQueryBuilder<ProductThreeDModel, ThreeDModelSearchParam> criteriaQueryBuilder) {
        this.organizationName = organizationName;
        this.threeDModelRepository = threeDModelRepository;
        this.organizationRepository = organizationRepository;
        this.securityService = securityService;
        this.roleService = roleService;
        this.employeeUserRepository = employeeUserRepository;
        this.productRepository = productRepository;
        this.mapper = mapper;
        this.fileService = fileService;
        this.subscriptionService = subscriptionService;
        this.criteriaQueryBuilder = criteriaQueryBuilder;
    }

    @Override
    public ThreeDModelResponse createNewThreeModel(String jsonString, MultipartFile[] files) throws JsonProcessingException {
        ThreeDModelDTO threeDModelDTO = mapper.readValue(jsonString, ThreeDModelDTO.class);
        validateUserWithOrganization(organizationName);
        validateBarcodeAndSKU(threeDModelDTO.getBarcode(), threeDModelDTO.getSku());
        ProductThreeDModel productThreeDModel = new ProductThreeDModel();
        threeDModelDTO.toEntity(productThreeDModel);
        ProductThreeDModel threeDModel = threeDModelRepository.save(productThreeDModel);
        MultipartFile[] filesTobeSaved = validateModelFiles(files);
        List<String> filesUrls = save3DModelFiles(filesTobeSaved, threeDModel.getId());
        return get3dModelResponse(threeDModel, filesUrls);
    }

    @Override
    public void assignModelToProduct(Long modelId, Long productId) {
        validate3DModelExisting(modelId);
        ProductEntity product = validateProductExisting(productId);
        product.setModelId(modelId);
        productRepository.save(product);
    }

    @Override
    public ThreeDModelResponse updateThreeDModel(Long modelId, String jsonString, MultipartFile[] files) throws JsonProcessingException {
        validateCurrentUserForEditing();
        ProductThreeDModel productThreeDModel = validate3DModelExisting(modelId);
        ThreeDModelDTO threeDModelDTO = mapper.readValue(jsonString, ThreeDModelDTO.class);
        validateUserWithOrganization(organizationName);
        if (threeDModelDTO.getBarcode() != null && !threeDModelDTO.getBarcode().equals(productThreeDModel.getBarcode())
                && threeDModelRepository.existsByBarcode(threeDModelDTO.getBarcode())) {
            throw new RuntimeBusinessException(CONFLICT, MODEL$005, "barcode");
        }
        if (threeDModelDTO.getSku() != null && !threeDModelDTO.getSku().equals(productThreeDModel.getSku())
                && threeDModelRepository.existsBySku(threeDModelDTO.getSku())) {
            throw new RuntimeBusinessException(CONFLICT, MODEL$006, "sku");
        }

        threeDModelDTO.toEntity(productThreeDModel);

        ProductThreeDModel threeDModel = threeDModelRepository.save(productThreeDModel);

        MultipartFile[] filesTobeSaved = validateModelFiles(files);
        List<String> filesUrls = save3DModelFiles(filesTobeSaved, threeDModel.getId());

        return get3dModelResponse(threeDModel, filesUrls);
    }

    @Override
    public ThreeDModelList getThreeDModelAll(ThreeDModelSearchParam searchParam) {
        setDefaultSearchParams(searchParam);
        CrieteriaQueryResults<ProductThreeDModel> results = criteriaQueryBuilder.getResultList(searchParam, true);
        List<ThreeDModelResponse> threeDModelResponses = new ArrayList<>();
        results.getResultList().forEach(threeDModel -> {
            List<String> fileUrls = fileService.getUrlsByModelId(threeDModel.getId());
            threeDModelResponses.add(get3dModelResponse(threeDModel, fileUrls));
        });
        return new ThreeDModelList(results.getResultCount(), threeDModelResponses);
    }

    @Override
    public ThreeDModelResponse getThreeDModelByBarcodeOrSKU(String barcode, String sku) {
        if (StringUtils.anyBlankOrNull(barcode) && StringUtils.anyBlankOrNull(sku)) {
            throw new RuntimeBusinessException(BAD_REQUEST, $003d$MODEL$, "barcode or sku");
        }
        ProductThreeDModel threeDModel = threeDModelRepository.findByBarcodeOrSku(barcode, sku);
        List<String> fileUrls = fileService.getUrlsByModelId(threeDModel.getId());
        return get3dModelResponse(threeDModel, fileUrls);
    }


    @Override
    public ThreeDModelResponse getThreeDModel(Long modelId) {
        if (modelId == null) {
            log.warn("there is no model found to assign it for the product");
            return null;
        }
        ProductThreeDModel threeDModel = threeDModelRepository.findById(modelId).orElseThrow(
                () -> new RuntimeBusinessException(NOT_FOUND, GEN$3dM$0002, modelId)
        );
        List<String> fileUrls = fileService.getUrlsByModelId(modelId);
        return get3dModelResponse(threeDModel, fileUrls);
    }

    @Override
    public void deleteThreeDModel(Long modelId) {
        validateCurrentUserForEditing();
        ProductThreeDModel threeDModel = validate3DModelExisting(modelId);
        List<String> filesUrls = fileService.getUrlsByModelId(modelId);
        filesUrls.forEach(fileService::deleteFileByUrl);
        threeDModelRepository.delete(threeDModel);
    }

    private List<String> save3DModelFiles(MultipartFile[] files, Long modelId) {
        List<String> filesUrls = new ArrayList<>();
        Arrays.stream(files).forEach(file -> {
            String url = fileService.saveFileFor3DModel(file, modelId);
            filesUrls.add(url);
        });
        return filesUrls;
    }

    void validateUserWithOrganization(String organizationName) {
        OrganizationEntity org = null;
        List<OrganizationEntity> organizations = organizationRepository.findByName(organizationName);
        if (organizations != null && !organizations.isEmpty())
            org = organizations.get(0);
        if (org == null) {
            OrganizationEntity organization = new OrganizationEntity();
            organization.setName(organizationName);
            organizationRepository.save(organization);
        } else {
            BaseUserEntity currentUser = securityService.getCurrentUser();
            EmployeeUserEntity employeeUserEntity = employeeUserRepository.findByIdAndOrganizationId(currentUser.getId(), org.getId()).orElseThrow(
                    () -> new RuntimeBusinessException(NOT_FOUND, E$USR$0005, currentUser.getId()));
            Roles userHighestRole = roleService.getEmployeeHighestRole(employeeUserEntity.getId());
            validateUserRoles(userHighestRole, currentUser);
        }

    }

    void validateCurrentUserForEditing() {
        BaseUserEntity currentUser = securityService.getCurrentUser();
        Roles userHighestRole = roleService.getEmployeeHighestRole(currentUser.getId());
        if (!userHighestRole.equals(Roles.NASNAV_ADMIN)) {
            throw new RuntimeBusinessException(FORBIDDEN, E$USR$0006, currentUser.getId());
        }
    }

    void validateUserRoles(Roles userHighestRole, BaseUserEntity currentUser) {
        if (userHighestRole == null) {
            log.error("there isn no roles for current user");
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, E$USR$0006, currentUser.getId());
        }
    }

    void validateOrganizationSubscription(SubscriptionInfoDTO subscriptionInfoDTO) {
        boolean isSubscribed = subscriptionInfoDTO.isSubscribed();
        if (isSubscribed) {
            throw new RuntimeBusinessException(NOT_ACCEPTABLE, ORG$SUB$0009);
        }
    }

    private ProductEntity validateProductExisting(Long productId) {
        return productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, P$PRO$0002, productId));
    }

    private ProductThreeDModel validate3DModelExisting(Long modelId) {
        return threeDModelRepository.findById(modelId)
                .orElseThrow(() -> new RuntimeBusinessException(NOT_FOUND, GEN$3dM$0002, modelId));
    }

    MultipartFile[] validateModelFiles(MultipartFile[] files) {
        if (files == null) {
            log.error("missing params you should enter at lest one file.");
            throw new RuntimeBusinessException(BAD_REQUEST, $004d$MODEL$, "file");
        }
        return files;
    }

    void validateBarcodeAndSKU(String barcode, String sku) {
        if (StringUtils.anyBlankOrNull(barcode) && StringUtils.anyBlankOrNull(sku)) {
            log.error("missing params you should enter barcode or sku.");
            throw new RuntimeBusinessException(BAD_REQUEST, $003d$MODEL$, "barcode or sku");
        }
        if (barcode != null && threeDModelRepository.existsByBarcode(barcode)) {
            throw new RuntimeBusinessException(CONFLICT, MODEL$005, "barcode");
        }
        if (sku != null && threeDModelRepository.existsBySku(sku)) {
            throw new RuntimeBusinessException(CONFLICT, MODEL$006, "sku");
        }
    }

    private void setDefaultSearchParams(ThreeDModelSearchParam searchParams) {
        if (searchParams.getStart() == null || searchParams.getStart() < 0) {
            searchParams.setStart(0);
        }
        if (searchParams.getCount() == null || (searchParams.getCount() < 1)) {
            searchParams.setCount(10);
        } else if (searchParams.getCount() > 1000) {
            searchParams.setCount(1000);
        }
    }
}
