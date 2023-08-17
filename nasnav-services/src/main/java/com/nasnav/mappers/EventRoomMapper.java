package com.nasnav.mappers;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.EventRoomResponse;
import com.nasnav.persistence.EventRoomTemplateEntity;

@Mapper(config = RoomMapper.class)
public interface EventRoomMapper {

	@InheritConfiguration
	@Mapping(target = "eventId", source = "event.id")
	@Mapping(target = "canStart", ignore = true)
	EventRoomResponse toResponse(EventRoomTemplateEntity entity);

	@InheritConfiguration(name = "toTemplateEntity")
	@Mapping(target = "event", ignore = true)
	EventRoomTemplateEntity toTemplateEntity(RoomTemplateDTO dto);

	@InheritConfiguration(name = "updateTemplateEntityfromDTO")
	@Mapping(target = "event", ignore = true)
	void updateTemplateEntityfromDTO(RoomTemplateDTO dto, @MappingTarget EventRoomTemplateEntity entity);
}
