package com.nasnav.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@AllArgsConstructor
@EqualsAndHashCode(callSuper=false)
public class ThemeClassDTO extends BaseRepresentationObject{
	
    private Integer id;
    private String name;

}
