package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.StringUtils;
import com.nasnav.dao.EmployeeUserRepository;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dao.ThreeDModelRepository;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.request.product.ThreeDModelDTO;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.nasnav.commons.utils.PagingUtils.getQueryPage;
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

    @Autowired
    public ThreeDModelServiceImpl(@Value("${organization_meetus_ar_name}") String organizationName,
                                  ThreeDModelRepository threeDModelRepository,
                                  OrganizationRepository organizationRepository, SecurityService securityService,
                                  RoleService roleService, EmployeeUserRepository employeeUserRepository,
                                  ProductRepository productRepository, ObjectMapper mapper, FileService fileService,
                                  @Lazy @Qualifier("wert") SubscriptionService subscriptionService) {
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
        ProductThreeDModel productThreeDModel = validate3DModelExisting(modelId);
        ThreeDModelDTO threeDModelDTO = mapper.readValue(jsonString, ThreeDModelDTO.class);
        validateUserWithOrganization(organizationName);
        validateBarcodeAndSKU(threeDModelDTO.getBarcode(), threeDModelDTO.getSku());

        threeDModelDTO.toEntity(productThreeDModel);

        ProductThreeDModel threeDModel = threeDModelRepository.save(productThreeDModel);

        MultipartFile[] filesTobeSaved = validateModelFiles(files);
        List<String> filesUrls = save3DModelFiles(filesTobeSaved, threeDModel.getId());

        return get3dModelResponse(threeDModel, filesUrls);
    }

    @Override
    public PageImpl<ThreeDModelResponse> getThreeDModelAll(Integer start, Integer count) {
        Page<ProductThreeDModel> response = threeDModelRepository.findAll(getQueryPage(start, count));
        List<ThreeDModelResponse> dtos = response.getContent().stream().map(l -> get3dModelResponse(l, fileService.getUrlsByModelId(l.getId())))
                .toList();
        return new PageImpl<>(dtos, response.getPageable(), response.getTotalElements());
    }

    @Override
    public ThreeDModelResponse getThreeDModelByBarcodeOrSKU(String barcode, String sku) {
        validateBarcodeAndSKU(barcode, sku);
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
    }
}
