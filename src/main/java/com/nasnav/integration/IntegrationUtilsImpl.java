package com.nasnav.integration;

import java.util.List;
import java.util.Map;

import com.nasnav.exceptions.BusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
	public Flux<ImportedImage> readImgsFromUrls(Map<String, List<VariantIdentifier>> imgToProductsMapping,
			ProductImageBulkUpdateDTO metaData
			, WebClient client) {
		return imgService.readImgsFromUrls(imgToProductsMapping, metaData, client);
	}


	@Override
	public void throwIntegrationInitException(String msg, Object... args) throws BusinessException {
		throw getIntegrationInitException(msg, args);
	}


	@Override
	public BusinessException getIntegrationInitException(String msg, Object... args) {
		return new BusinessException( String.format( msg, args )
				, "INTEGRATION INITIALIZATION FAILURE"
				, HttpStatus.INTERNAL_SERVER_ERROR);
	}

}
