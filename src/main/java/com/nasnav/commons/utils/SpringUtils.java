package com.nasnav.commons.utils;

import java.io.IOException;
import java.nio.file.Files;

import org.springframework.core.io.Resource;

public class SpringUtils {
	public static String readResource(Resource resource) throws IOException {
    	return new String( Files.readAllBytes(resource.getFile().toPath()) );
    }
}
