package com.nasnav.exceptions;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class ImageImportBulkErrorResponse {
	private List<String> errors;
}
