package com.nasnav.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import com.nasnav.dto.response.PackageResponse;
import com.nasnav.persistence.PackageEntity;

import java.util.LinkedList;
import java.util.List;

@Mapper(componentModel = "spring")
public interface PackageMapper {
	PackageMapper INSTANCE = Mappers.getMapper(PackageMapper.class);

	PackageResponse toPackageResponse(PackageEntity entity);


	default List<PackageResponse> entitiesToBeansWithoutList(List<PackageEntity> entities){
		if(entities == null) return new LinkedList<>();
		List<PackageResponse> beans = new LinkedList<>();
		for (PackageEntity entity : entities){
			beans.add(this.toPackageResponse(entity));
		}
		return beans;
	}
}
