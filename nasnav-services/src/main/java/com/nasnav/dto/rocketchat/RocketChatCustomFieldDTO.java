package com.nasnav.dto.rocketchat;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class RocketChatCustomFieldDTO {
	private String key;
	private String value;
	private boolean overwrite;

	public RocketChatCustomFieldDTO(String key, String value) {
		this(key, value, true);
	}
}
