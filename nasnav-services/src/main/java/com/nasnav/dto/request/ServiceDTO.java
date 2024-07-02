package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.persistence.ServiceEntity;
import lombok.Data;

@Data
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ServiceDTO {
    private String code;
    private String name;
    private String description;
    private String lightLogo;
    private String darkLogo;
    private Boolean enabled;

    public static void toEntity(ServiceDTO serviceDTO, ServiceEntity serviceEntity) {
        if (serviceDTO.getCode() != null) {
            serviceEntity.setCode(serviceDTO.getCode());
        }
        if (serviceDTO.getName() != null) {
            serviceEntity.setName(serviceDTO.getName());
        }
        if (serviceDTO.getDescription() != null) {
            serviceEntity.setDescription(serviceDTO.getDescription());
        }
        if (serviceDTO.getLightLogo() != null) {
            serviceEntity.setLightLogo(serviceDTO.getLightLogo());
        }
        if (serviceDTO.getDarkLogo() != null) {
            serviceEntity.setDarkLogo(serviceDTO.getDarkLogo());
        }
        if (serviceDTO.getEnabled() != null) {
            serviceEntity.setEnabled(serviceDTO.getEnabled());
        }
    }
}
