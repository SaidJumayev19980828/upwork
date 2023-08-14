package com.nasnav.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import com.nasnav.dto.request.RoomTemplateDTO;
import com.nasnav.dto.response.ShopRoomResponse;
import com.nasnav.persistence.ShopRoomTemplateEntity;

@Mapper(componentModel = "spring")
public interface RoomMapper {
	RoomMapper INSTANCE = Mappers.getMapper(RoomMapper.class);

	@Mapping(target = "shop", source = "shop.representation")
	@Mapping(target =  "sessionExternalId", source = "session.externalId")
	@Mapping(target = "sessionCreatedAt", source = "session.createdAt")
	ShopRoomResponse toShopRoomResponse(ShopRoomTemplateEntity entity);

	@Mapping(target = "shop", ignore = true)
	@Mapping(target = "session", ignore = true)
	@Mapping(target = "id", ignore = true)
	ShopRoomTemplateEntity toRoomTemplateEntity(RoomTemplateDTO dto);

	@Mapping(target = "shop", ignore = true)
	@Mapping(target = "session", ignore = true)
	@Mapping(target = "id", ignore = true)
	void updateTemplateEntityfromDTO(RoomTemplateDTO dto, @MappingTarget ShopRoomTemplateEntity entity);
}
