package com.nasnav.service.impl;

import static java.util.Collections.emptyList;
import static java.util.Objects.nonNull;

import java.util.List;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.SwatchImageBulkUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ProductImageUpdateResponse;
import com.nasnav.service.FeatureImageService;
import com.nasnav.service.ImagesBulkService;
import com.nasnav.service.ProductImageService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ImagesBulkServiceImpl implements ImagesBulkService {
	private final ProductImageService productImageService;
	private final FeatureImageService featureImageService;

	@Override
	@Transactional(rollbackFor = Throwable.class)
	public List<ProductImageUpdateResponse> updateImagesBulk(@Valid MultipartFile zip, @Valid MultipartFile csv,
			@Valid ProductImageBulkUpdateDTO metaData) throws BusinessException {
		if (nonNull(metaData.getFeatureId())) {
			SwatchImageBulkUpdateDTO swatchMetaData = new SwatchImageBulkUpdateDTO(metaData);
			featureImageService.updateSwatchImagesBulk(zip, csv, swatchMetaData);
			return emptyList();
		}
		return productImageService.updateProductImageBulk(zip, csv, metaData);
	}
}
