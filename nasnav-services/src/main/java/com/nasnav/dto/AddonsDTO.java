package com.nasnav.dto;


import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AddonsDTO  extends BaseJsonDTO{
	private Long id;
    private Integer type ;
    private String name;
    private String operation;
	@Override
	protected void initRequiredProperties() {
		
		
	}
   
}
