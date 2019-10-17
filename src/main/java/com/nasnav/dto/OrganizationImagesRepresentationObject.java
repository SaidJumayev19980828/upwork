package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper = false)
@ApiModel(value = "OrganizationImage")
public class OrganizationImagesRepresentationObject extends BaseRepresentationObject{

    @ApiModelProperty(value = "ID key identifying the Image")
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(value = "ID of its organization")
    @JsonProperty("org_id")
    private Long organizationId;

    @ApiModelProperty(value = "ID of its shop")
    @JsonProperty("shop_id")
    private Long shopId;

    @ApiModelProperty(value = "Type ID")
    @JsonProperty("type")
    private Integer type;

    @ApiModelProperty(value = "url to the Image")
    @JsonProperty("url")
    private String uri;
}

