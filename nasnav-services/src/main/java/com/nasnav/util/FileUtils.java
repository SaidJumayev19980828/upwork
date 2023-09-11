package com.nasnav.util;

import static com.google.common.io.Files.getFileExtension;
import static com.nasnav.exceptions.ErrorCodes.P$IMG$0001;
import static com.nasnav.exceptions.ErrorCodes.P$IMG$0002;
import static lombok.AccessLevel.PRIVATE;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;

import org.springframework.web.multipart.MultipartFile;

import com.nasnav.exceptions.RuntimeBusinessException;

import lombok.NoArgsConstructor;

@NoArgsConstructor(access = PRIVATE)
public final class FileUtils {
	public static void validateImgBulkZip(MultipartFile zip){
		if(zip.isEmpty()) {
			throw new RuntimeBusinessException(
					NOT_ACCEPTABLE
					, P$IMG$0001);
		}
		String ext = getFileExtension(zip.getOriginalFilename());
		if(!ext.equalsIgnoreCase("zip")) {
			throw new RuntimeBusinessException(
					NOT_ACCEPTABLE
					, P$IMG$0002);
		}
	}
}
