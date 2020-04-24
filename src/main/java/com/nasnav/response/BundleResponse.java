package com.nasnav.response;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.BundleDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper=false)
@NoArgsConstructor
public class BundleResponse{
	
	private Long total;
	private List<BundleDTO> bundles;
	
	public BundleResponse(Long total, List<BundleDTO> bundles ) {
		this();
		this.total = total;
		this.bundles = bundles;
	}
	
}
