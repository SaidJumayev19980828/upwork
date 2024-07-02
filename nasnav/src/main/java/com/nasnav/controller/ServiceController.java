package com.nasnav.controller;

import com.nasnav.dto.OrganizationServicesDto;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;
import com.nasnav.service.ServiceInterface;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@CrossOrigin("*")
@RestController
@RequiredArgsConstructor
@Log4j2
@RequestMapping("/service")
public class ServiceController {

    private final ServiceInterface service;

    @PostMapping()
    public ServiceResponse createService(@RequestHeader(name = "User-Token", required = false) String userToken,
                                         @RequestBody ServiceDTO serviceDTO) {
        return service.createService(serviceDTO);
    }

    @PutMapping("/{id}")
    public ServiceResponse updateService(@RequestHeader(name = "User-Token", required = false) String userToken,
                                         @PathVariable(name = "id") Long id,
                                         @RequestBody ServiceDTO serviceDTO) {
        return service.updateService(id, serviceDTO);
    }

    @DeleteMapping("/{id}")
    public void deleteService(@RequestHeader(name = "User-Token", required = false) String userToken,
                              @PathVariable(name = "id") Long id) {
        service.deleteService(id);
    }

    @GetMapping("/{id}")
    public ServiceResponse getService(@RequestHeader(name = "User-Token", required = false) String userToken,
                                      @PathVariable(name = "id") Long id) {
        return service.getService(id);
    }

    @GetMapping()
    public List<ServiceResponse> getService(@RequestHeader(name = "User-Token", required = false) String userToken) {
        return service.getAllServices();
    }

    @GetMapping("/org/{orgId}")
    public List<ServiceResponse> getOrgServices(@RequestHeader(name = "User-Token", required = false) String userToken,
                                               @PathVariable(name = "orgId") Long orgId) {
        return service.getOrgServiceResponses(orgId);
    }

    @GetMapping("/org_service")
    public List<OrganizationServicesDto> getOrgServices(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                        @RequestParam(name = "orgId", required = false) Long orgId,
                                                        @RequestParam(name = "serviceId", required = false) Long serviceId) {
        return service.getOrgServices(orgId, serviceId);
    }

    @PutMapping("/org")
    public void updateOrgServices(@RequestHeader(name = "User-Token", required = false) String userToken,
                                  @RequestBody OrganizationServicesDto request) {
        service.updateOrgService(request);
    }

    @PostMapping("/enable")
    public void enablePackageServicesForOrganization(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                     @RequestParam(name = "packageId") Long packageId,
                                                     @RequestParam(name = "orgId") Long orgId) {
        service.enablePackageServicesForOrganization(packageId, orgId);
    }

}
