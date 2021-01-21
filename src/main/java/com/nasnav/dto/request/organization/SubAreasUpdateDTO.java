package com.nasnav.dto.request.organization;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.dto.SubAreaDTO;
import lombok.Data;

import java.util.List;

@Data
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SubAreasUpdateDTO {
    private List<SubAreaDTO> subAreas;
}
