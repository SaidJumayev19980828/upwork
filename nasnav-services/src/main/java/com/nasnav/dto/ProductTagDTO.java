package com.nasnav.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ProductTagDTO extends BaseRepresentationObject{

    @JsonProperty("products_ids")
    List<Long> productIds;
    @JsonProperty("tags_ids")
    List<Long> tagIds;

}
