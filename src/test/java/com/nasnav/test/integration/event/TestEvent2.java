package com.nasnav.test.integration.event;

import java.util.function.Consumer;

import com.nasnav.integration.events.Event;
import com.nasnav.integration.events.EventResult;

public class TestEvent2 extends Event<Long, String> {

	public TestEvent2(Long organizationId, Long eventData, Consumer<EventResult<Long, String>> onSuccess) {
		super(organizationId, eventData, onSuccess);
	}

}
