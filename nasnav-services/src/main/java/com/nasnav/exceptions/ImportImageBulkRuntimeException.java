package com.nasnav.exceptions;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportImageBulkRuntimeException extends RuntimeException {

	private static final long serialVersionUID = 54542361L;
	private List<String> errors;
	
	public ImportImageBulkRuntimeException(List<String> errors) {
		this.errors = errors;
	}
	

	public ImportImageBulkRuntimeException(Throwable e, List<String> errors) {
		super(e);
		this.errors = errors;
	}

}
