package com.nasnav.controller;

import com.nasnav.service.ShippingManagementService;
import com.nasnav.service.VideoChatService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static java.lang.String.format;

@RestController
@RequestMapping("/callbacks")
@CrossOrigin("*")
public class CallbackController {
	
	private final Logger logger = LogManager.getLogger();

    @Autowired
    private ShippingManagementService shippingService;

	@Autowired
	private VideoChatService videoChatService;

    @PostMapping(value = "/shipping/service/{service_id}/{org_id}",
                 consumes = MediaType.ALL_VALUE)
    public void shippingCallback(@PathVariable("service_id") String serviceId,
                                 @PathVariable("org_id") Long orgId,
                                 @RequestBody String dto) throws IOException {
        logger.info("Shipping Service [%s] for org[%d] sent a callback with body[%s]", serviceId, orgId, dto);
        shippingService.updateShipmentStatus(serviceId, orgId, dto);
    }

	@PostMapping(value = "/videochat/", consumes = MediaType.APPLICATION_JSON_VALUE)
	public void videoChatCallback(@RequestBody String dto) {
		videoChatService.handelCallbackEvent(dto);
	}
}
