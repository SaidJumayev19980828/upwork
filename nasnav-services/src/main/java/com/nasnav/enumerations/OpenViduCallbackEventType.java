package com.nasnav.enumerations;

import lombok.Getter;

@Getter
public enum OpenViduCallbackEventType {
	sessionCreated,
	sessionDestroyed ,
	participantJoined,
	participantLeft,
	webrtcConnectionCreated,
	webrtcConnectionDestroyed,
	recordingStatusChanged,
	filterEventDispatched,
	signalSent;
}
