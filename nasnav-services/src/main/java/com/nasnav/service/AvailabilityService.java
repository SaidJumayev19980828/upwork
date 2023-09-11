package com.nasnav.service;

import com.nasnav.dto.UserRepresentationObject;
import com.nasnav.dto.request.AvailabilityDTO;
import com.nasnav.persistence.AvailabilityEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface AvailabilityService {
    public List<AvailabilityDTO> overrideAvailabilities(AvailabilityDTO dto, boolean forceFlag);

    public List<AvailabilityDTO> shiftUpcomingAppointments(List<Long> ids, long postponedPeriodsInMins);

    public AvailabilityEntity reserveAvailability(Long id);

    public void cancelReserveAvailability(Long id);

    public void deleteAvailabilitiesByRange(LocalDateTime startsAt ,LocalDateTime endsAt, boolean forceFlag);

    public AvailabilityEntity getById(Long id);

    public List<AvailabilityDTO> getAllFreeAvailabilitiesByOrg(Long orgId);

    public List<AvailabilityDTO> getAllFreeAvailabilitiesByOrgAndEmployee(Long orgId, Long employeeId);

    public Set<UserRepresentationObject> getAllEmployeesWithOrWithoutSlotsByOrg(Long orgId, boolean availableSlots);

    public List<AvailabilityDTO> getAllFreeAvailabilitiesByShop(Long shopId);

    public List<AvailabilityDTO> getAllOccupiedAvailabilitiesByLoggedEmployee();

    public List<AvailabilityDTO> getAllAppointmentsByLoggedUser();

    public List<AvailabilityDTO> getAllAppointmentsByUserId(Long id);


}
