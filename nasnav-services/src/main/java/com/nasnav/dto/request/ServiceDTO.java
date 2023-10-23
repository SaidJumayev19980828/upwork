package com.nasnav.dto.request;

import lombok.Data;

import javax.persistence.Column;
import java.math.BigDecimal;
import java.util.Set;

@Data
public class ServiceDTO {
    private String code;
    private String name;
    private String description;
    public ServiceDTO(){}
    public ServiceDTO(String code){
        this.code = code;
    }


}
