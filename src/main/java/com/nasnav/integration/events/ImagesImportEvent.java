package com.nasnav.integration.events;

import java.util.Set;
import java.util.function.Consumer;

import com.nasnav.integration.events.data.ImageImportParam;
import com.nasnav.service.model.ImportedImage;

public class ImagesImportEvent extends Event<ImageImportParam, Set<ImportedImage>> {

	public ImagesImportEvent(Long organizationId, ImageImportParam eventData) {
		super(organizationId, eventData);
	}
	
	
	
	public ImagesImportEvent(Long organizationId, ImageImportParam eventData
			, Consumer<EventResult<ImageImportParam, Set<ImportedImage>>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
