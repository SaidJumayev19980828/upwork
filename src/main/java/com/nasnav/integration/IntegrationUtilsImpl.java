package com.nasnav.integration;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.dto.ProductImageBulkUpdateDTO;
import com.nasnav.service.ProductImageService;
import com.nasnav.service.model.ImportedImage;
import com.nasnav.service.model.VariantIdentifier;

import reactor.core.publisher.Flux;

@Service
public class IntegrationUtilsImpl implements IntegrationUtils{
	
	
	@Autowired
	private ProductImageService imgService;
	

	@Override
	public Flux<ImportedImage> readImgsFromUrls(Map<String, List<VariantIdentifier>> fileIdentifiersMap,
			ProductImageBulkUpdateDTO metaData
			, WebClient client) {
		return imgService.readImgsFromUrls(fileIdentifiersMap, metaData, client);
	}

}
