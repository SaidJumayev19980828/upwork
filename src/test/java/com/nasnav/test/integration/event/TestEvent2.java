package com.nasnav.test.integration.event;

import com.nasnav.integration.events.Event;

public class TestEvent2 extends Event<Long, String> {

	public TestEvent2(Long organizationId, Long eventData) {
		super(organizationId, eventData);
	}

}
