package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import springfox.documentation.annotations.ApiIgnore;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "name",
        "p_name",
        "logo_url",
        "parent_id"
})

@Data
@EqualsAndHashCode(callSuper=true)
public class CategoryRepresentationObject extends BaseRepresentationObject{

    @ApiModelProperty(value = "updating Category id", example = "123", required = false)
    @JsonProperty("id")
    private Long id;

    @ApiModelProperty(value = "Category Name", example = "Perfume", required = false)
    @JsonProperty("name")
    private String name;

    @JsonIgnore
    @JsonProperty("p_name")
    private String pname;

    @ApiModelProperty(value = "Category Logo Url", example = "/categories/logo/logo_12.jpg", required = false)
    @JsonProperty("logo_url")
    private String logo;

    @ApiModelProperty(value = "Category's parent Id", example = "123", required = false)
    @JsonProperty("parent_id")
    private Integer parentId;

    @ApiModelProperty(value = "Operation Type", example = "create", required = true)
    @JsonProperty("operation")
    private String operation;
}
