package com.nasnav.service.model;

import lombok.Getter;
import lombok.Setter;
import org.springframework.core.io.UrlResource;

import java.net.MalformedURLException;
import java.net.URI;

public class FileUrlResource extends UrlResource {
	
	private static final String DEFAULT_MIME_TYPE = "application/octet-stream";
	
	@Getter @Setter
	private String mimeType;

	@Getter @Setter
	private String filename;

	public FileUrlResource(URI uri, String mimeType , String fileName) throws MalformedURLException {
		super(uri);		
		this.mimeType = mimeType != null ? mimeType : DEFAULT_MIME_TYPE;
		this.filename = fileName;
	}

}
