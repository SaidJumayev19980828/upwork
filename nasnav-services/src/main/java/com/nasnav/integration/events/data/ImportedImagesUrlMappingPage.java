package com.nasnav.integration.events.data;

import com.nasnav.service.model.VariantIdentifier;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.springframework.web.reactive.function.client.WebClient;

import java.util.List;
import java.util.Map;

@Data
@AllArgsConstructor
public class ImportedImagesUrlMappingPage {
	private Integer total;
	private Map<String, List<VariantIdentifier>> imgToProductsMapping;
	private WebClient imgsWebClient;
}
