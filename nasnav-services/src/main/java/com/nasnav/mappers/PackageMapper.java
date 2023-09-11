package com.nasnav.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.nasnav.dto.response.PackageResponse;
import com.nasnav.persistence.PackageEntity;

@Mapper(componentModel = "spring")
public interface PackageMapper {
	PackageMapper INSTANCE = Mappers.getMapper(PackageMapper.class);

	PackageResponse toPackageResponse(PackageEntity entity);
}
