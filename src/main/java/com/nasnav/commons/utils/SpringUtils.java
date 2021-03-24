package com.nasnav.commons.utils;

import static com.google.common.base.Charsets.UTF_8;
import static java.util.Optional.ofNullable;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.core.io.Resource;

import com.google.common.io.ByteSource;

public class SpringUtils {
	private static final Logger log = LogManager.getLogger();

	public static String readResource(Resource resource){
		ByteSource byteSource = new ByteSource() {
	        @Override
	        public InputStream openStream() throws IOException {
	            return resource.getInputStream();
	        }
	    };
		try {
			return byteSource.asCharSource(UTF_8).read();
		} catch (IOException e) {
			log.error(e,e);
			return "";
		}
	}




	public static Optional<String> readOptionalResource(Resource resource){
		ByteSource byteSource = new ByteSource() {
			@Override
			public InputStream openStream() throws IOException {
				return resource.getInputStream();
			}
		};
		try {
			return ofNullable(byteSource.asCharSource(UTF_8).read());
		} catch (IOException e) {
			log.error(e,e);
			return Optional.empty();
		}
	}
}
