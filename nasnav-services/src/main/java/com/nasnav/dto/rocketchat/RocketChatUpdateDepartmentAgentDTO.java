package com.nasnav.dto.rocketchat;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Singular;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RocketChatUpdateDepartmentAgentDTO {
	@Singular("upsert")
	private List<RocketChatDepartmentAgentDTO> upsert;
	@Singular("remove")
	private List<RocketChatDepartmentAgentDTO> remove;
}
