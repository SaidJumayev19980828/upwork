package com.nasnav.exceptions;

import com.nasnav.response.ImportProcessStatusResponse;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DataImportAsyncException extends Exception {
	private static final long serialVersionUID = 54816154735L;
	private final transient ImportProcessStatusResponse statusResponse;
}
