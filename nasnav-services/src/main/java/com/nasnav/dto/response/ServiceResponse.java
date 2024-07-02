package com.nasnav.dto.response;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.persistence.ServiceEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public class ServiceResponse extends ServiceDTO {
    private Long id;

    public static ServiceResponse from(ServiceEntity serviceEntity) {
        ServiceResponse serviceResponse = new ServiceResponse();
        serviceResponse.setId(serviceEntity.getId());
        serviceResponse.setCode(serviceEntity.getCode());
        serviceResponse.setName(serviceEntity.getName());
        serviceResponse.setDescription(serviceEntity.getDescription());
        serviceResponse.setLightLogo(serviceEntity.getLightLogo());
        serviceResponse.setDarkLogo(serviceEntity.getDarkLogo());
        serviceResponse.setEnabled(serviceEntity.getEnabled());
        return serviceResponse;
    }
}
