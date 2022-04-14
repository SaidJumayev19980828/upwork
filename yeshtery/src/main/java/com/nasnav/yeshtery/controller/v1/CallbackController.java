package com.nasnav.yeshtery.controller.v1;

import com.nasnav.service.ShippingManagementService;
import com.nasnav.yeshtery.YeshteryConstants;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

import static java.lang.String.format;

@RestController
@RequestMapping(CallbackController.API_PATH)
@CrossOrigin("*")
public class CallbackController {
    static final String API_PATH = YeshteryConstants.API_PATH +"/callbacks";
	
	private final Logger logger = LogManager.getLogger();

    @Autowired
    private ShippingManagementService shippingService;

    @PostMapping(value = "/shipping/service/{service_id}/{org_id}",
                 consumes = MediaType.ALL_VALUE)
    public void shippingCallback(@PathVariable("service_id") String serviceId,
                                 @PathVariable("org_id") Long orgId,
                                 @RequestBody String dto) throws IOException {
    	logger.info(format("Shipping Service [%s] for org[%d] sent a callback with body[%s]", serviceId, orgId, dto));
        shippingService.updateShipmentStatus(serviceId, orgId, dto);
    }
}
