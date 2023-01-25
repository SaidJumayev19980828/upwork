package com.nasnav.controller;

import com.nasnav.dto.request.AvailabilityDTO;
import com.nasnav.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import static com.nasnav.constatnts.EntityConstants.TOKEN_HEADER;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

@RestController
@RequestMapping(value = "/availability", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
@CrossOrigin("*")
public class AvailabilityController {
    @Autowired
    private AvailabilityService availabilityService;

    @PostMapping
    public List<AvailabilityDTO> createOrOverrideAvailabilities(@RequestHeader(TOKEN_HEADER) String userToken,
            @RequestBody AvailabilityDTO dto,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        return availabilityService.overrideAvailabilities(dto, force);
    }

    @PostMapping(value = "/shift/{period}")
    public List<AvailabilityDTO> shiftAvailability(@RequestBody List<Long> ids, @PathVariable Long period){
        return availabilityService.shiftUpcomingAppointments(ids, period);
    }

    @DeleteMapping
    public void deleteAvailabilitiesByRange(@RequestHeader(TOKEN_HEADER) String userToken,
            @RequestBody AvailabilityDTO dto,
            @RequestParam(required = false, defaultValue = "false") boolean force) {
        availabilityService.deleteAvailabilitiesByRange(dto, force);
    }

    @GetMapping(value = "/shop/{shopId}")
    public List<AvailabilityDTO> getAllByShop(@RequestHeader(TOKEN_HEADER) String userToken,
            @PathVariable Long shopId) {
        return availabilityService.getAllFreeAvailabilitiesByShop(shopId);
    }

    @GetMapping(value = "/org/{orgId}")
    public List<AvailabilityDTO> getAllFreeAvailabilitiesByOrg(@RequestHeader(TOKEN_HEADER) String userToken,
            @PathVariable long orgId) {
        return availabilityService.getAllFreeAvailabilitiesByOrg(orgId);
    }

    @GetMapping(value = "/employee")
    public List<AvailabilityDTO> getAllOccupiedAvailabilitiesByLoggedEmployee(@RequestHeader(TOKEN_HEADER) String userToken){
        return availabilityService.getAllOccupiedAvailabilitiesByLoggedEmployee();
    }

    @GetMapping(value = "/user")
    public List<AvailabilityDTO> getAllAppointmentsByLoggedUser(@RequestHeader(TOKEN_HEADER) String userToken){
        return availabilityService.getAllAppointmentsByLoggedUser();
    }

    @GetMapping(value = "/user/{userId}")
    public List<AvailabilityDTO> getAllAppointmentsByUserId(@RequestHeader(TOKEN_HEADER) String userToken,
            @PathVariable Long userId) {
        return availabilityService.getAllAppointmentsByUserId(userId);
    }
}
