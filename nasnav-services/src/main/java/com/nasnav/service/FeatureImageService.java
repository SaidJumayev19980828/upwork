package com.nasnav.service;

import java.util.Set;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.dto.ImportedSwatchImage;
import com.nasnav.dto.SwatchImageBulkUpdateDTO;

public interface FeatureImageService {

	void saveSwatchImagesBulk(Set<ImportedSwatchImage> importedImgs, SwatchImageBulkUpdateDTO metaData);

	void updateSwatchImagesBulk(
			MultipartFile zip, MultipartFile csv, SwatchImageBulkUpdateDTO metaData);

}