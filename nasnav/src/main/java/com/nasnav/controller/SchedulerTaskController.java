package com.nasnav.controller;

import com.nasnav.service.SchedulerTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/appointment", produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class SchedulerTaskController {
    @Autowired
    private SchedulerTaskService schedulerTaskService;

    @PostMapping(value = "/{availabilityId}")
    public void createAppointment(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @PathVariable Long availabilityId) {
        schedulerTaskService.createAppointment(availabilityId);
    }

    @DeleteMapping(value = "/{id}")
    public void cancelAppointmentForCustomer(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken, @PathVariable Long id) {
        schedulerTaskService.deleteAppointmentFromCustomer(id);
    }

}