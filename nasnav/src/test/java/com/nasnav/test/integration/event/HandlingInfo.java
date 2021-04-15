package com.nasnav.test.integration.event;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class HandlingInfo {
	private Thread thread;
	private LocalDateTime handlingStartTime;
	private Boolean callBackExecuted;
}
