package com.nasnav.request;

import lombok.Data;

@Data
public class GetIntegrationErrorParam {
	
	//had to ignore java naming convention, because the bundle request parameters will be mapped to these properties by name
	private Long org_id;
	private String event_type;
	private Integer page_size;
	private Integer page_num;
}
