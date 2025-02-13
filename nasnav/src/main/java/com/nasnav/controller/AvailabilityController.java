package com.nasnav.controller;


import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.AvailabilityDTO;
import com.nasnav.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = "/availability", produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class AvailabilityController {
    @Autowired
    private AvailabilityService availabilityService;

    @PostMapping
    public List<AvailabilityDTO> createOrOverrideAvailabilities(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                                                @RequestBody AvailabilityDTO dto,
                                                                @RequestParam(required = false, defaultValue = "false") boolean force) {
        return availabilityService.overrideAvailabilities(dto, force);
    }

    @PostMapping(value = "/shift/{period}")
    public List<AvailabilityDTO> shiftAvailability(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @RequestBody List<Long> ids,
            @PathVariable Long period) {
        return availabilityService.shiftUpcomingAppointments(ids, period);
    }

    @DeleteMapping
    public void deleteAvailabilitiesByRange(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @RequestParam("starts_at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startsAt,
            @RequestParam("ends_at") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endsAt,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        availabilityService.deleteAvailabilitiesByRange(startsAt, endsAt, force);
    }

    @GetMapping(value = "/shop/{shopId}")
    public List<AvailabilityDTO> getAllByShop(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @PathVariable Long shopId) {
        return availabilityService.getAllFreeAvailabilitiesByShop(shopId);
    }

    @GetMapping(value = "/org/{orgId}")
    public List<AvailabilityDTO> getAllFreeAvailabilitiesByOrg(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @PathVariable long orgId) {
        return availabilityService.getAllFreeAvailabilitiesByOrg(orgId);
    }

    @GetMapping(value = "/org/{orgId}/{employeeId}")
    public List<AvailabilityDTO> getAllFreeAvailabilitiesByOrgAndEmployee(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                                                          @PathVariable long orgId,
                                                                          @PathVariable long employeeId) {
        return availabilityService.getAllFreeAvailabilitiesByOrgAndEmployee(orgId, employeeId);
    }

    @GetMapping(value = "/employee/slots")
    public Set<UserRepresentationObject> getAllEmployeesWithOrWithoutSlotsByOrg(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
                                                                                @RequestParam long orgId,
                                                                                @RequestParam boolean availableSlots ){
        return availabilityService.getAllEmployeesWithOrWithoutSlotsByOrg(orgId, availableSlots);
    }

    @GetMapping(value = "/employee")
    public List<AvailabilityDTO> getAllOccupiedAvailabilitiesByLoggedEmployee(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken){
        return availabilityService.getAllOccupiedAvailabilitiesByLoggedEmployee();
    }

    @GetMapping(value = "/user")
    public List<AvailabilityDTO> getAllAppointmentsByLoggedUser(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken){
        return availabilityService.getAllAppointmentsByLoggedUser();
    }

    @GetMapping(value = "/user/{userId}")
    public List<AvailabilityDTO> getAllAppointmentsByUserId(@RequestHeader(value = TOKEN_HEADER, required = false) String userToken,
            @PathVariable Long userId) {
        return availabilityService.getAllAppointmentsByUserId(userId);
    }
}
