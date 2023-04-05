package com.nasnav.dao;

import com.nasnav.persistence.EventLogsEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface EventLogsRepository extends CrudRepository<EventLogsEntity, Long> {
    boolean existsByEvent_IdAndUser_IdOrEmployee_Id(Long eventId, Long userId, Long employeeId);
    List<EventLogsEntity> getAllByEvent_Id(Long id);
    void deleteAllByEvent_Id(Long eventId);
    @Query("select event from EventLogsEntity event where event.event.id =:eventId")
    PageImpl<EventLogsEntity> getAllByEventIdPageable(Long eventId,Pageable page);
    int countByEvent_Influencer_Id(Long influencerId);
    int countByEvent_Influencer_IdAndAttendAtNotNull(Long influencerId);
}
