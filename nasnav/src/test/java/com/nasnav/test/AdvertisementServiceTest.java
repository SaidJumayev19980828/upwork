package com.nasnav.test;

import com.nasnav.dao.AdvertisementRepository;
import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.ProductBaseInfo;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.mappers.AdvertisementMapper;
import com.nasnav.mappers.AdvertisementMapperImpl;
import com.nasnav.persistence.AdvertisementEntity;
import com.nasnav.persistence.ProductEntity;
import com.nasnav.service.AdvertisementService;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.impl.AdvertisementServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AdvertisementServiceTest {
    private AdvertisementService advertisementService;
    @Mock
    private AdvertisementRepository advertisementRepository;
    @Mock
    private ProductRepository productRepository;
    @Mock
    private ProductImageService imageService;
    private AdvertisementMapper advertisementMapper = new AdvertisementMapperImpl();

    private final PageImpl<AdvertisementEntity> advertisementEntities = new PageImpl<>(List.of(createAdvertisementEntity()));
    private final String imageUrl = "random-image-url";
    private final AdvertisementEntity advertisementEntity = createAdvertisementEntity();
    private final ProductEntity productEntity = createProductEntity();

    @BeforeEach
    void reInit() {
        this.advertisementService = new AdvertisementServiceImpl(advertisementRepository, imageService, productRepository, advertisementMapper);
        Mockito.when(advertisementRepository.findAll(any(PageRequest.class))).thenReturn(advertisementEntities);
        Mockito.when(advertisementRepository.save(any(AdvertisementEntity.class))).thenReturn(advertisementEntity);
        Mockito.when(productRepository.getById(any(Long.class))).thenReturn(productEntity);
        Mockito.when(imageService.getProductCoverImage(any(Long.class))).thenReturn(imageUrl);
    }

    private static AdvertisementEntity createAdvertisementEntity() {
        AdvertisementEntity advertisementEntity = new AdvertisementEntity();
        advertisementEntity.setProduct(createProductEntity());
        return advertisementEntity;
    }

    private static ProductEntity createProductEntity() {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId(1L);
        productEntity.setName("name");
        return productEntity;
    }

    @Test
    void testFindAllAdvertisements() {
        PageImpl<AdvertisementDTO> allAdvertisements = advertisementService.findAllAdvertisements(0, 10);
        assertThat(allAdvertisements.getContent().size(), equalTo(advertisementEntities.getSize()));
    }

    @Test
    void testCreateAdvertisements() {
        AdvertisementDTO advertisementDTO = new AdvertisementDTO();
        ProductBaseInfo product = new ProductBaseInfo();
        product.setId(1L);
        advertisementDTO.setProduct(product);
        AdvertisementDTO dto = advertisementService.create(advertisementDTO);
        assertThat(dto.getProduct().getImageUrl(), equalTo(imageUrl));
    }

}