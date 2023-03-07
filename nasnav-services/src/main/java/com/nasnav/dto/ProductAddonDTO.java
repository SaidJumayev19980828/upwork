package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ProductAddonDTO extends BaseRepresentationObject{

    @JsonProperty("products_ids")
    List<Long> productIds;
    @JsonProperty("addons_ids")
    List<Long> addonIds;

}
