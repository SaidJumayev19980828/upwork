package com.nasnav.enumerations;

import lombok.Getter;

@Getter
public enum OpenViduCallbackReason {
	lastParticipantLeft,
	sessionClosedByServer,
	mediaServerDisconnect,
	nodeCrashed,
	disconnect,
	forceDisconnectByUser,
	forceDisconnectByServer,
	networkDisconnect,
	openviduServerStopped;
}
