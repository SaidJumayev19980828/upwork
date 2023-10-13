package com.nasnav.mappers;


import com.nasnav.dto.response.ChatWidgetSettingResponse;
import com.nasnav.persistence.ChatWidgetSettingEntity;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.stereotype.Component;


@Component
@Mapper(componentModel = "spring")
public interface ChatWidgetSettingMapper {
    @Mapping(source = "organization.id", target = "organizationId")
    ChatWidgetSettingResponse toResponse(ChatWidgetSettingEntity entity);
}
