package com.nasnav.integration.events;

import java.util.function.Consumer;

import com.nasnav.integration.events.data.ImageImportParam;
import com.nasnav.integration.events.data.ImportedImagesUrlMappingPage;

public class ImagesImportEvent extends Event<ImageImportParam, ImportedImagesUrlMappingPage> {

	public ImagesImportEvent(Long organizationId, ImageImportParam eventData) {
		super(organizationId, eventData);
	}
	
	
	
	public ImagesImportEvent(Long organizationId, ImageImportParam eventData
			, Consumer<EventResult<ImageImportParam, ImportedImagesUrlMappingPage>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
