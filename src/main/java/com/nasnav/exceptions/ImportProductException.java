package com.nasnav.exceptions;

import com.nasnav.service.model.importproduct.context.ImportProductContext;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportProductException extends Exception {
	private static final long serialVersionUID = 21652135L;
	private ImportProductContext context;
	
	
	public ImportProductException(ImportProductContext context) {
		super(context.getErrors().stream().findFirst().map(e -> e.getMessage()).orElse("Error"));
		this.context = context;
		
	}
	
	
	public ImportProductException(Throwable e, ImportProductContext context) {
		super(e);
		this.context = context;
	}
	
}

