package com.nasnav.service.model;

import com.nasnav.dto.ProductImageUpdateDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImportedImage {
	private MultipartFile image;
	private ProductImageUpdateDTO imgMetaData;
	private String path;
}
