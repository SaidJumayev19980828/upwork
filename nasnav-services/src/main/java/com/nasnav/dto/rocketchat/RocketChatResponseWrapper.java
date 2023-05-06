package com.nasnav.dto.rocketchat;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper=true)
public class RocketChatResponseWrapper<T extends RocketChatWrappedData> extends RocketChatDTOWrapper<T> {
	Boolean success;
}
