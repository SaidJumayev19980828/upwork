package com.nasnav.dto.rocketchat;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketChatDepartmentAgentDTO {
	private String agentId;
	private String username;
	private String departmentId;
	private Integer count;
	private Integer order;
}
