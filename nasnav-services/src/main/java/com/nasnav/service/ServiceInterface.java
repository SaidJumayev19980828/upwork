package com.nasnav.service;

import com.nasnav.dto.OrganizationServicesDto;
import com.nasnav.dto.request.ServiceDTO;
import com.nasnav.dto.response.ServiceResponse;

import java.util.List;

public interface ServiceInterface {
    ServiceResponse createService(ServiceDTO service);

    ServiceResponse updateService(Long serviceId, ServiceDTO service);

    void deleteService(Long serviceId);

    ServiceResponse getService(Long serviceId);

    List<ServiceResponse> getAllServices();

    List<ServiceResponse> getOrgServiceResponses(Long orgId);

    List<OrganizationServicesDto> getOrgServices(Long orgId, Long serviceId);

    void updateOrgService(OrganizationServicesDto request);

    void enablePackageServicesForOrganization(Long packageId, Long orgId);

}
