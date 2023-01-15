package com.nasnav.service;

import com.nasnav.persistence.AvailabilityEntity;

import java.util.List;

public interface SchedulerTaskService {
    /**
     * this method creates a new appointment
     * should handling adding the appointment to the task-scheduler
     */
    public void createAppointment(Long availabilityId);

    /**
     * this method should delete the task from shcudler as well as removing it
     */
    public void deleteAppointmentFromCustomer(Long availabilityId);

    public void deleteAppointmentFromEmployee(List<AvailabilityEntity> entities);

    public void updateAppointment(Long availabilityId);

    public void overrideAppointment(List<AvailabilityEntity> deletedEntities, List<AvailabilityEntity> newEntities);

}
