package com.nasnav.request;

import com.nasnav.integration.enums.MappingType;
import lombok.Data;

@Data
public class GetIntegrationDictParam {
	//had to ignore java naming convention, because the bundle request parameters will be mapped to these properties by name
	private Long org_id;
	private Integer page_size;
	private Integer page_num;
	private MappingType dict_type;
}
