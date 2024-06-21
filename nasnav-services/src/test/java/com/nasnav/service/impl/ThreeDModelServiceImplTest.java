package com.nasnav.service.impl;

import com.nasnav.dao.ThreeDModelRepository;
import com.nasnav.dto.SubscriptionInfoDTO;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.ProductThreeDModel;
import com.nasnav.service.FileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.junit.Assert.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.*;
import static org.mockito.Mockito.times;

class ThreeDModelServiceImplTest {

    @Mock
    private ThreeDModelRepository threeDModelRepository;

    @Mock
    private FileService fileService;

    @InjectMocks
    private ThreeDModelServiceImpl threeDModelService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    void getThreeDModel_ModelExists() {
        // Arrange
        Long modelId = 1L;
        ProductThreeDModel threeDModel = new ProductThreeDModel();
        threeDModel.setId(modelId);

        when(threeDModelRepository.findById(modelId)).thenReturn(Optional.of(threeDModel));
        when(fileService.getUrlsByModelId(modelId)).thenReturn(Arrays.asList("url1", "url2"));

        // Act
        ThreeDModelResponse response = threeDModelService.getThreeDModel(modelId);

        // Assert
        assertNotNull(response);
        verify(threeDModelRepository, times(1)).findById(modelId);
        verify(fileService, times(1)).getUrlsByModelId(modelId);
    }

    @Test
    void getThreeDModel_ModelDoesNotExist() {
        // Arrange
        Long modelId = 1L;

        when(threeDModelRepository.findById(modelId)).thenReturn(Optional.empty());

        // Act
        ThreeDModelResponse response = threeDModelService.getThreeDModel(modelId);

        // Assert
        assertNull(response);
        verify(threeDModelRepository, times(1)).findById(modelId);
        verify(fileService, times(0)).getUrlsByModelId(modelId);
    }


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
    void validateBarcodeAndSKUTest() {
        String barcode = "123456";
        String sku = "123456";
        assertThatCode(() -> threeDModelService.validateBarcodeAndSKU(barcode, sku))
                .doesNotThrowAnyException();
    }

    @Test
    void validateBarcodeAndSKUBarcodeExistsTest() {
        String barcode = "123456";
        String sku = "123456";
        ProductThreeDModel threeDModel = new ProductThreeDModel();
        threeDModel.setBarcode(barcode);
        when(threeDModelRepository.existsByBarcode(barcode)).thenReturn(true);
        when(threeDModelRepository.existsBySku(sku)).thenReturn(false);
        assertThatCode(() -> threeDModelService.validateBarcodeAndSKU(barcode, sku))
                .isInstanceOf(RuntimeBusinessException.class);
    }

    @Test
    void validateBarcodeAndSKUSkuExistsTest() {
        String barcode = "123456";
        String sku = "123456";
        ProductThreeDModel threeDModel = new ProductThreeDModel();
        threeDModel.setBarcode(barcode);
        when(threeDModelRepository.existsByBarcode(barcode)).thenReturn(false);
        when(threeDModelRepository.existsBySku(sku)).thenReturn(true);
        assertThatCode(() -> threeDModelService.validateBarcodeAndSKU(barcode, sku))
                .isInstanceOf(RuntimeBusinessException.class);
    }

}