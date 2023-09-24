package com.nasnav.dto.response;

import com.nasnav.dto.request.PackageDTO;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)
public class PackageResponse extends PackageDTO {
	private Long id;
}