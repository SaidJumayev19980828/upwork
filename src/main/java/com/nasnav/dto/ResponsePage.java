package com.nasnav.dto;

import static java.util.Collections.emptyList;

import java.util.List;

import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;


@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ResponsePage<T> extends PageImpl<T> {

	
	
	public ResponsePage() {
		this(emptyList());
	}
	
	
	public ResponsePage(List<T> content) {
		super(content);
	}
	
	
	public ResponsePage(List<T> content, ResponsePageable pageable, long total) {
		super(content, (Pageable)pageable, total);
	}
		

}
