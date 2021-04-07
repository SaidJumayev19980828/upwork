package com.nasnav.integration;

import java.util.List;
import java.util.Map;

import com.nasnav.exceptions.BusinessException;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.service.model.ImportedImage;
import com.nasnav.service.model.VariantIdentifier;

import reactor.core.publisher.Flux;

public interface IntegrationUtils {

	public Flux<ImportedImage> readImgsFromUrls(	
			Map<String, List<VariantIdentifier>> fileIdentifiersMap
			, ProductImageBulkUpdateDTO metaData
			, WebClient client);

	public void throwIntegrationInitException(String msg, Object... args) throws BusinessException;

	public BusinessException getIntegrationInitException(String msg, Object... args);
}
