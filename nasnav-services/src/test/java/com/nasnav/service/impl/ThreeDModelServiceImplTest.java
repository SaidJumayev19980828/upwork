package com.nasnav.service.impl;

import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.enumerations.Roles;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.UserEntity;
import org.junit.jupiter.api.Test;
import org.springframework.web.multipart.MultipartFile;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.jupiter.api.Assertions.assertNull;

class ThreeDModelServiceImplTest {

    private final ThreeDModelServiceImpl threeDModelService = ThreeDModelServiceImpl.builder().build();

    @Test
    void validateBarcodeAndSKU() {
        String barcode = null;
        String sku = null;
        assertThatCode(() -> threeDModelService.validateBarcodeAndSKU(barcode, sku))
                .isInstanceOf(RuntimeBusinessException.class);
    }

    @Test
    void validateModelFiles() {
        MultipartFile[] files = null;
        assertThatCode(() -> threeDModelService.validateModelFiles(files))
                .isInstanceOf(RuntimeBusinessException.class);
    }

    @Test
    void validateOrganizationSubscription() {
        SubscriptionInfoDTO subscriptionInfoDTO = new SubscriptionInfoDTO();
        subscriptionInfoDTO.setSubscribed(true);
        assertThatCode(() -> threeDModelService.validateOrganizationSubscription(subscriptionInfoDTO))
                .isInstanceOf(RuntimeBusinessException.class);
    }

    @Test
    void testNullableThreeDModel() {
        Long modelId = null;
        assertNull(threeDModelService.getThreeDModel(modelId));
    }

    @Test
    void validateUserRoles() {
        Roles userHighestRole = null;
        BaseUserEntity currentUser = new UserEntity();
        currentUser.setId(12L);
        assertThatCode(() -> threeDModelService.validateUserRoles(userHighestRole, currentUser))
                .isInstanceOf(RuntimeBusinessException.class);
    }

}