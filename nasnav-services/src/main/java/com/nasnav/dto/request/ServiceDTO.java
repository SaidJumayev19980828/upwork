package com.nasnav.dto.request;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Set;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ServiceDTO {
    private String code;
    private String name;
    private String description;
    private String lightLogo;
    private String darkLogo;
    private Boolean enabled;
    public ServiceDTO(){}
    public ServiceDTO(String code){
        this.code = code;
    }


}
