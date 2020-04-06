package com.nasnav.integration.events.data;

import java.util.List;
import java.util.Map;

import org.springframework.web.reactive.function.client.WebClient;

import com.nasnav.service.model.VariantIdentifier;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImportedImagesUrlMappingPage {
	private Integer total;
	private Map<String, List<VariantIdentifier>> fileIdentifiersMap;
	private WebClient imgsWebClient;
}
