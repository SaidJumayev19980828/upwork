package com.nasnav.exceptions;

import com.nasnav.service.model.importproduct.context.ImportProductContext;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ImportProductRuntimeException extends RuntimeException{
	private static final long serialVersionUID = 21652135L;
	private ImportProductContext context;
	
	public ImportProductRuntimeException(Throwable e, ImportProductContext context) {
		super(e);
		this.context = context;
	}
}
