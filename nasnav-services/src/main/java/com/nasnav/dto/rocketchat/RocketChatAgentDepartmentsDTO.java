package com.nasnav.dto.rocketchat;

import java.util.ArrayList;

public class RocketChatAgentDepartmentsDTO extends ArrayList<RocketChatDepartmentAgentDTO> implements RocketChatWrappedData {

	@Override
	public String getFieldName() {
		return "departments";
	}
	
}
