package com.nasnav.integration.events.data;

import java.util.Set;

import com.nasnav.service.model.ImportedImage;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportedImagesPage {
	private Integer total;
	private Set<ImportedImage> images;
}
