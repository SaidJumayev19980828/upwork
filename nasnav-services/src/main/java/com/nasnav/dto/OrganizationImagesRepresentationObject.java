package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;


@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@EqualsAndHashCode(callSuper = false)
@Schema(name = "OrganizationImage")
public class OrganizationImagesRepresentationObject extends BaseRepresentationObject{

    @Schema(name = "ID key identifying the Image")
    @JsonProperty("id")
    private Long id;

    @Schema(name = "ID of its organization")
    @JsonProperty("org_id")
    private Long organizationId;

    @Schema(name = "ID of its shop")
    @JsonProperty("shop_id")
    private Long shopId;

    @Schema(name = "Type ID")
    @JsonProperty("type")
    private Integer type;

    @Schema(name = "url to the Image")
    @JsonProperty("url")
    private String uri;
}

