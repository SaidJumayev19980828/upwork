package com.nasnav.mappers;

import org.mapstruct.MapperConfig;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.RoomResponse;
import com.nasnav.persistence.RoomTemplateEntity;

@MapperConfig(componentModel = "spring")
public interface RoomMapper {

	@Mapping(target = "sessionExternalId", source = "session.externalId")
	@Mapping(target = "sessionCreatedAt", source = "session.createdAt")
	@Mapping(target = "started", expression = "java(entity.isStarted())")
	RoomResponse toResponse(RoomTemplateEntity entity);

	@Mapping(target = "session", ignore = true)
	@Mapping(target = "id", ignore = true)
	RoomTemplateEntity toTemplateEntity(RoomTemplateDTO dto);

	@Mapping(target = "session", ignore = true)
	@Mapping(target = "id", ignore = true)
	void updateTemplateEntityfromDTO(RoomTemplateDTO dto, @MappingTarget RoomTemplateEntity entity);
}
