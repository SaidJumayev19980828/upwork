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
	@Builder.Default
	private Integer count = 0;
	@Builder.Default
	private Integer order = 0;
}
