package com.nasnav.test.integration.event;

import java.util.function.Consumer;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;

public class TestEventWithNoHandler extends Event<String, String> {

	public TestEventWithNoHandler(Long organizationId, String eventData
			, Consumer<EventResult<String, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
