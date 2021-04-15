package com.nasnav.integration;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.model.ImportedImage;
import com.nasnav.service.model.VariantIdentifier;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

public interface IntegrationUtils {

	public Flux<ImportedImage> readImgsFromUrls(	
			Map<String, List<VariantIdentifier>> fileIdentifiersMap
			, ProductImageBulkUpdateDTO metaData
			, WebClient client);

	public void throwIntegrationInitException(String msg, Object... args) throws BusinessException;

	public BusinessException getIntegrationInitException(String msg, Object... args);
}
