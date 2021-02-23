package com.nasnav.dto;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.enumerations.ProductFeatureType;
import com.nasnav.persistence.ProductFeaturesEntity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import static com.nasnav.enumerations.ProductFeatureType.STRING;
import static java.util.Collections.emptyMap;

@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class ProductFeatureDTO extends BaseRepresentationObject{
	private Integer id;
    private String name;    
    private String description;
    
    @JsonProperty(value = "p_name")
    private String pname;

    private Integer level;

    private ProductFeatureType type;

    private Map<String, ?> extraData;
}
