package com.nasnav.test.integration.event;

import com.nasnav.integration.events.Event;

public class TestEventWithNoHandler extends Event<String, String> {

	public TestEventWithNoHandler(Long organizationId, String eventData) {
		super(organizationId, eventData);
	}

}
