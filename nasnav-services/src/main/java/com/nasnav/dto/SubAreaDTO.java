package com.nasnav.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.math.BigDecimal;


@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@EqualsAndHashCode(callSuper = false)
public class SubAreaDTO extends BaseJsonDTO{
    private Long id;
    private String name;
    private Long areaId;
    private BigDecimal latitude;
    private BigDecimal longitude;

    @Override
    protected void initRequiredProperties() {}

    void setName(String name) {
        setPropertyAsUpdated("name");
        this.name = name;
    }
}
