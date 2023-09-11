package com.nasnav.dao;

import com.nasnav.persistence.EventLogsEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Temporal;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.TemporalType;
import java.time.LocalDate;
import java.util.Date;
import java.util.List;

public interface EventLogsRepository extends CrudRepository<EventLogsEntity, Long> {
    boolean existsByEvent_IdAndUser_IdOrEmployee_Id(Long eventId, Long userId, Long employeeId);
    List<EventLogsEntity> getAllByEvent_Id(Long id);
    void deleteAllByEvent_Id(Long eventId);
    @Query("select event from EventLogsEntity event where event.event.id =:eventId")
    PageImpl<EventLogsEntity> getAllByEventIdPageable(Long eventId,Pageable page);
    int countByEvent_Influencer_Id(Long influencerId);
    int countByEvent_Influencer_IdAndAttendAtNotNull(Long influencerId);
    @Query("select COUNT(*) from EventLogsEntity event where event.event.influencer.id = :influencerId and CAST(event.interestedAt as date) = :dateFilter and (:orgId is null or event.event.organization.id = :orgId)")
    int countInterests(Long influencerId, @DateTimeFormat(pattern="yyyy-MM-dd")Date dateFilter, Long orgId);
    @Query("select COUNT(*) from EventLogsEntity event where event.event.influencer.id = :influencerId and CAST(event.attendAt as date) = :dateFilter and (:orgId is null or event.event.organization.id = :orgId)")
    int countAttends(Long influencerId, @DateTimeFormat(pattern="yyyy-MM-dd")Date dateFilter, Long orgId);
}
