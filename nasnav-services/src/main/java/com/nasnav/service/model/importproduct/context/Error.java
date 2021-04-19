package com.nasnav.service.model.importproduct.context;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Error{
	@JsonIgnore
	private Throwable exception;
	private String data;
	private Integer rowNum;
	private String message;
	private String stackTrace;
	
	public Error(Throwable exception, String data, Integer rowNum) {
		this.data = data;
		this.rowNum = rowNum;
		this.exception = exception;
		
		StringBuilder msg = new StringBuilder();
	    msg.append(String.format("Error at Row[%d], with data[%s]", rowNum + 1, data));
	    msg.append(System.getProperty("line.separator"));
	    msg.append("Error Message: " + exception.getMessage());
	    
	    this.message = msg.toString();
	}
	
	
	public Error(String message, Integer rowNum) {
		this.message = message;
		this.rowNum = rowNum;
	}
}