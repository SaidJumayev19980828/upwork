package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import org.springframework.data.domain.Page;

import java.util.List;

import static java.util.Collections.emptyList;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ResponsePage<T> {
	private Long totalElements;
	private Integer pageNumber;
	private Integer pageSize;
	private List<T> content;
	private Integer totalPages;
	
	
	
	
	public ResponsePage() {
		totalElements = 0L;
		pageNumber = 0;
		pageSize = 0;
		content  = emptyList();		
	}
	
	
	
	public ResponsePage(List<T> content, Integer pageSize, Integer pageNumber) {
		this.content = content;
		this.pageSize = pageSize;
		this.pageNumber = pageNumber;
	}
	
	
	
	public ResponsePage(Page<T> page) {
		this.content = page.getContent();
		this.pageSize = page.getSize();
		this.pageNumber = page.getNumber();
		this.totalElements = page.getTotalElements();
		this.totalPages = page.getTotalPages();
	}
}
