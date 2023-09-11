package com.nasnav.dao;

import com.nasnav.persistence.AvailabilityEntity;
import com.nasnav.persistence.SchedulerTaskEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SchedulerTaskRepository extends JpaRepository<SchedulerTaskEntity,Long> {
    List<SchedulerTaskEntity> findAllByAvailability(AvailabilityEntity availabilityEntity);
    List<SchedulerTaskEntity> findAllByAvailabilityIn(List<AvailabilityEntity> availabilityEntities);
}
