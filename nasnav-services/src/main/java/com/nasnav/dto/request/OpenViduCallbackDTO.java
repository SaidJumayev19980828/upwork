package com.nasnav.dto.request;

import com.nasnav.enumerations.OpenViduCallbackEventType;
import com.nasnav.enumerations.OpenViduCallbackReason;
import lombok.Data;

@Data
public class OpenViduCallbackDTO {
	private OpenViduCallbackEventType event;
	private String sessionId;
	private String connectionId;
	private Long duration;
	private OpenViduCallbackReason reason;
	private String clientData;
}
