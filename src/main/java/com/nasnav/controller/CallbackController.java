package com.nasnav.controller;

import com.nasnav.service.ShippingManagementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController
@RequestMapping("/callbacks")
@CrossOrigin("*")
public class CallbackController {

    @Autowired
    private ShippingManagementService shippingService;

    @PostMapping(value = "/shipping/service/{service_id}/{org_id}",
                 consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public void shippingCallback(@PathVariable("service_id") String serviceId,
                                 @PathVariable("org_id") Long orgId,
                                 @RequestBody String dto) throws IOException {
        shippingService.updateShipmentStatus(serviceId, orgId, dto);
    }
}
