package com.nasnav.service.impl;

import com.nasnav.dao.SchedulerTaskRepository;
import com.nasnav.persistence.AvailabilityEntity;
import com.nasnav.persistence.BaseUserEntity;
import com.nasnav.persistence.SchedulerTaskEntity;
import com.nasnav.persistence.UserEntity;
import com.nasnav.service.AvailabilityService;
import com.nasnav.service.SchedulerTaskService;
import com.nasnav.service.SecurityService;
import com.nasnav.service.scheduler.ScheduleTaskHelper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class SchedulerTaskServiceImpl implements SchedulerTaskService {
    @Autowired
    private SecurityService securityService;
    @Autowired
    private SchedulerTaskRepository schedulerTaskRepository;
    @Autowired
    private ScheduleTaskHelper scheduleTaskHelper;
    @Autowired
    private AvailabilityService availabilityService;

    @Transactional
    @Override
    public void createAppointment(Long availabilityId) {
        AvailabilityEntity availabilityEntity = availabilityService.reserveAvailability(availabilityId);
        availabilityEntity.setUser(getUser());
        SchedulerTaskEntity schedulerTaskEntity = new SchedulerTaskEntity(availabilityEntity);
        schedulerTaskEntity = schedulerTaskRepository.save(schedulerTaskEntity);
        scheduleTaskHelper.sendMailAndAddTaskToScheduler(schedulerTaskEntity);
    }

    @Transactional
    @Override
    public void deleteAppointmentFromCustomer(Long availabilityId) {
        AvailabilityEntity availabilityEntity = availabilityService.getById(availabilityId);
        List<SchedulerTaskEntity> entities = schedulerTaskRepository.findAllByAvailability(availabilityEntity);

        schedulerTaskRepository.deleteAll(entities);
        entities.stream().forEach((entity) -> {
            scheduleTaskHelper.removeTaskFromScheduler(entity);
            availabilityService.cancelReserveAvailability(entity.getAvailability().getId());
        });
    }

    @Override
    public void deleteAppointmentFromEmployee(List<AvailabilityEntity> entities) {
        List<SchedulerTaskEntity> entitiesToDelete = schedulerTaskRepository.findAllByAvailabilityIn(entities);
        schedulerTaskRepository.deleteAll(entitiesToDelete);
        entitiesToDelete.stream().forEach((entity) -> {
            scheduleTaskHelper.removeTaskFromScheduler(entity);
        });
    }

    @Override
    public void updateAppointment(Long availabilityId) {
        AvailabilityEntity availabilityEntity = availabilityService.getById(availabilityId);
        List<SchedulerTaskEntity> entities = schedulerTaskRepository.findAllByAvailability(availabilityEntity);
        entities.stream().forEach((entity) -> {
            scheduleTaskHelper.removeTaskFromScheduler(entity);
            entity.setStartsAt(availabilityEntity.getStartsAt());
            scheduleTaskHelper.addTaskToScheduler(entity);
        });
        schedulerTaskRepository.saveAll(entities);
    }

    @Override
    public void overrideAppointment(List<AvailabilityEntity> deletedEntities, List<AvailabilityEntity> newEntities) {
        deleteAppointmentFromEmployee(deletedEntities);

        List<SchedulerTaskEntity> entitiesToSave = new ArrayList<>();
        newEntities.stream().forEach((entity) -> {
            SchedulerTaskEntity taskEntity = new SchedulerTaskEntity(entity);
            entitiesToSave.add(taskEntity);
            scheduleTaskHelper.addTaskToScheduler(taskEntity);
        });
        schedulerTaskRepository.saveAll(entitiesToSave);
    }

    private UserEntity getUser(){
        BaseUserEntity loggedInUser = securityService.getCurrentUser();
        if(loggedInUser instanceof UserEntity){
            return (UserEntity) loggedInUser;
        }
        return null;
    }

}
