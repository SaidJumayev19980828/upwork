package com.nasnav.test.integration.event;

import java.util.function.Consumer;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;

public class TestEventWithHandlerInfo extends Event<Integer,HandlingInfo> {

	public TestEventWithHandlerInfo(Long organizationId, Integer eventData
			, Consumer<EventResult<Integer, HandlingInfo>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
