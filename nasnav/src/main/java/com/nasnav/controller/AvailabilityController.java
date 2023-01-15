package com.nasnav.controller;

import com.nasnav.dto.request.AvailabilityDTO;
import com.nasnav.service.AvailabilityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/availability")
@CrossOrigin("*")
public class AvailabilityController {
    @Autowired
    private AvailabilityService availabilityService;

    @PostMapping(value = "/{forceFlag}")
    public List<AvailabilityDTO> createOrOverrideAvailabilities(@RequestBody AvailabilityDTO dto,@PathVariable boolean forceFlag){
        return availabilityService.overrideAvailabilities(dto,forceFlag);
    }

    @PostMapping(value = "/shift/{period}")
    public List<AvailabilityDTO> shiftAvailability(@RequestBody List<Long> ids, @PathVariable Long period){
        return availabilityService.shiftUpcomingAppointments(ids, period);
    }

    @DeleteMapping("/{forceFlag}")
    public void deleteAvailabilitiesByRange(@RequestBody AvailabilityDTO dto, @PathVariable boolean forceFlag){
        availabilityService.deleteAvailabilitiesByRange(dto, forceFlag);
    }

    @GetMapping(value = "/shop/{shopId}")
    public List<AvailabilityDTO> getAllByShop(@PathVariable Long shopId){
        return availabilityService.getAllFreeAvailabilitiesByShop(shopId);
    }

    @GetMapping("/org/{orgId}")
    public List<AvailabilityDTO> getAllFreeAvailabilitiesByOrg(@PathVariable long orgId){
        return availabilityService.getAllFreeAvailabilitiesByOrg(orgId);
    }

    @GetMapping(value = "/employee")
    public List<AvailabilityDTO> getAllOccupiedAvailabilitiesByLoggedEmployee(){
        return availabilityService.getAllOccupiedAvailabilitiesByLoggedEmployee();
    }

    @GetMapping(value = "/user")
    public List<AvailabilityDTO> getAllAppointmentsByLoggedUser(){
        return availabilityService.getAllAppointmentsByLoggedUser();
    }

    @GetMapping(value = "/user/{userId}")
    public List<AvailabilityDTO> getAllAppointmentsByUserId(@PathVariable Long userId){
        return availabilityService.getAllAppointmentsByUserId(userId);
    }
}
