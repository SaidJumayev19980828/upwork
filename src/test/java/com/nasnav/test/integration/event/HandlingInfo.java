package com.nasnav.test.integration.event;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class HandlingInfo {
	private Thread thread;
	private LocalDateTime handlingStartTime;
	private Boolean callBackExecuted;
}
