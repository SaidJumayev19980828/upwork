package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "OrganizationImage")
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@NoArgsConstructor
public class OrganizationImagesRepresentationObject extends BaseRepresentationObject{

    @ApiModelProperty(value = "ID key identifying the Image")
    private Long id;

    @ApiModelProperty(value = "ID of its organization")
    @JsonProperty("org_id")
    private Long organizationId;

    @ApiModelProperty(value = "ID of its shop")
    private Long shopId;

    @ApiModelProperty(value = "Type ID")
    private Integer type;

    @ApiModelProperty(value = "url to the Image")
    @JsonProperty("url")
    private String uri;

    private String mimeType;

    public OrganizationImagesRepresentationObject(Long id, Integer type, String uri, String mimeType) {
        this.id = id;
        this.type = type;
        this.uri = uri;
        this.mimeType = mimeType;
    }
}

