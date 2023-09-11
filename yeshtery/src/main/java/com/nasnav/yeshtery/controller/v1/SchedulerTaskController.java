package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.service.SchedulerTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = SchedulerTaskController.API_PATH, produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class SchedulerTaskController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/appointment";

    @Autowired
    private SchedulerTaskService schedulerTaskService;

    @PostMapping(value = "/{availabilityId}")
    public void createAppointment(@RequestHeader(TOKEN_HEADER) String userToken, @PathVariable Long availabilityId) {
        schedulerTaskService.createAppointment(availabilityId);
    }

    @DeleteMapping(value = "/{id}")
    public void cancelAppointmentForCustomer(@RequestHeader(TOKEN_HEADER) String userToken, @PathVariable Long id) {
        schedulerTaskService.deleteAppointmentFromCustomer(id);
    }

}