package com.nasnav.exceptions;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ImageImportBulkErrorResponse {
	private List<String> errors;
}
