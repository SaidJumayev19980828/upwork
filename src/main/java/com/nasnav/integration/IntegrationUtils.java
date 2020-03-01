package com.nasnav.integration;

import java.util.List;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.dto.ProductImageUpdateIdentifier;
import com.nasnav.service.model.ImportedImage;

import reactor.core.publisher.Flux;

public interface IntegrationUtils {

	public Flux<ImportedImage> readImgsFromUrls(	
			Map<String, List<ProductImageUpdateIdentifier>> fileIdentifiersMap
			, ProductImageBulkUpdateDTO metaData
			, WebClient client);
}
