package com.nasnav.test.integration.event;

import com.nasnav.integration.events.Event;

public class TestEventWithHandlerInfo extends Event<Integer,HandlingInfo> {

	public TestEventWithHandlerInfo(Long organizationId, Integer eventData) {
		super(organizationId, eventData);
	}

}
