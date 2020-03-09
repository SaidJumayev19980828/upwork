package com.nasnav.commons.utils;

import static com.google.common.base.Charsets.UTF_8;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.core.io.Resource;

import com.google.common.io.ByteSource;

public class SpringUtils {
	public static String readResource(Resource resource) throws IOException {
		ByteSource byteSource = new ByteSource() {
	        @Override
	        public InputStream openStream() throws IOException {
	            return resource.getInputStream();
	        }
	    };
		
    	return byteSource.asCharSource(UTF_8).read();
    }
}
