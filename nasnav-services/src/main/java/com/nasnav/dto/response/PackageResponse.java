package com.nasnav.dto.response;

import com.nasnav.dto.request.PackageDto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PackageResponse extends PackageDto {
	private Long id;
}