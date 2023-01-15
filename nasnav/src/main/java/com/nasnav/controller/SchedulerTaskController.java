package com.nasnav.controller;

import com.nasnav.service.SchedulerTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/appointment")
@CrossOrigin("*")
public class SchedulerTaskController {
    @Autowired
    private SchedulerTaskService schedulerTaskService;

    @PostMapping(value = "/{availabilityId}")
    public void createAppointment(@PathVariable Long availabilityId) {
        schedulerTaskService.createAppointment(availabilityId);
    }

    @DeleteMapping(value = "/{id}")
    public void cancelAppointmentForCustomer(@PathVariable Long id) {
        schedulerTaskService.deleteAppointmentFromCustomer(id);
    }

}