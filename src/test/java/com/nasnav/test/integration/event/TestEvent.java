package com.nasnav.test.integration.event;

import com.nasnav.integration.events.Event;

public class TestEvent extends Event<String, String> {

	public TestEvent(Long organizationId, String eventData) {
		super(organizationId, eventData);
	}

}
