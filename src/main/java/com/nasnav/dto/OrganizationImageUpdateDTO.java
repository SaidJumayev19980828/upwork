package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.nasnav.constatnts.EntityConstants.Operation;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OrganizationImageUpdateDTO extends BaseJsonDTO {

    private Long shopId;
    @JsonProperty(value = "org_id")
    private Long organizationId;
    private Long imageId;
    private Operation operation;
    private Integer type;

    @Override
    protected void initRequiredProperties() {
        setPropertyAsRequired("organizationId", Required.ALWAYS);
        setPropertyAsRequired("operation", Required.ALWAYS);
        setPropertyAsRequired("imageId", Required.FOR_UPDATE);
        setPropertyAsRequired("type", Required.FOR_CREATE);
    }

    public void setOrganizationId(Long organizationId) {
        setPropertyAsUpdated("organizationId");
        this.organizationId = organizationId;
    }

    public void setShopId(Long shopId) {
        setPropertyAsUpdated("shopId");
        this.shopId = shopId;
    }

    public void setImageId(Long imageId) {
        setPropertyAsUpdated("imageId");
        this.imageId = imageId;
    }

    public void setOperation(Operation operation) {
        setPropertyAsUpdated("operation");
        this.operation = operation;
    }

    public void setType(Integer type) {
        setPropertyAsUpdated("type");
        this.type = type;
    }

}
