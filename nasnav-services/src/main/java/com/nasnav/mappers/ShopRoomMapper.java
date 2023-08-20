package com.nasnav.mappers;

import org.mapstruct.InheritConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.ShopRoomResponse;
import com.nasnav.persistence.ShopRoomTemplateEntity;

@Mapper(config = RoomMapper.class)
public interface ShopRoomMapper {

	@InheritConfiguration
	@Mapping(target = "shop", source = "shop.representation")
	@Mapping(target = "canStart", constant = "true")
	ShopRoomResponse toResponse(ShopRoomTemplateEntity entity);

	@InheritConfiguration(name = "toTemplateEntity")
	@Mapping(target = "shop", ignore = true)
	ShopRoomTemplateEntity toTemplateEntity(RoomTemplateDTO dto);

	@InheritConfiguration(name = "updateTemplateEntityfromDTO")
	@Mapping(target = "shop", ignore = true)
	void updateTemplateEntityfromDTO(RoomTemplateDTO dto, @MappingTarget ShopRoomTemplateEntity entity);
}
