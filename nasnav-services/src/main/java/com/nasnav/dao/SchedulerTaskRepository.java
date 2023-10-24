package com.nasnav.dao;

import com.nasnav.persistence.AvailabilityEntity;
import com.nasnav.persistence.SchedulerTaskEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;


public interface SchedulerTaskRepository extends JpaRepository<SchedulerTaskEntity,Long> {
    List<SchedulerTaskEntity> findAllByAvailability(AvailabilityEntity availabilityEntity);
    List<SchedulerTaskEntity> findAllByAvailabilityIn(List<AvailabilityEntity> availabilityEntities);

    //TODO
    PageImpl<SchedulerTaskEntity> findAllByStartsAtBetween(LocalDateTime startDateTime, LocalDateTime endDateTime , Pageable page);
}
