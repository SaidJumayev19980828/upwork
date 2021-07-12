package com.nasnav.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=false)

public class ExtraAttributesRepresentationObject extends BaseRepresentationObject{

    private Integer id;
    private String name;
    private String type;
    private String iconUrl;
}
