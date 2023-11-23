package com.nasnav.dao;

import com.nasnav.dto.InterestEventInfo;
import com.nasnav.persistence.EventLogsEntity;
import com.nasnav.persistence.InfluencerEntity;
import com.nasnav.persistence.SchedulerTaskEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface EventLogsRepository extends CrudRepository<EventLogsEntity, Long> {
    boolean existsByEvent_IdAndUser_IdOrEvent_IdAndEmployee_Id(Long eventId, Long userId, Long eventId2, Long employeeId);

    List<EventLogsEntity> getAllByEvent_Id(Long id);
    void deleteAllByEvent_Id(Long eventId);
    @Query("select event from EventLogsEntity event where event.event.id =:eventId")
    PageImpl<EventLogsEntity> getAllByEventIdPageable(Long eventId,Pageable page);

    @Query("select COUNT(*) from EventLogsEntity eventLog  JOIN eventLog.event.influencers influencer where influencer.id = :influencerId ")
    int countByEvent_InfluencersContains(Long influencerId);

    @Query("select COUNT(*) from EventLogsEntity eventLog  JOIN eventLog.event.influencers influencer where influencer.id = :influencerId and CAST(eventLog.attendAt as date) is Not Null ")
    int countByEvent_InfluencersContainsAndAttendAtNotNull(Long influencerId);
//    @Query("select COUNT(*) from EventLogsEntity event where event.event.influencer.id = :influencerId and CAST(event.interestedAt as date) = :dateFilter and (:orgId is null or event.event.organization.id = :orgId)")

    @Query("select COUNT(*) from EventLogsEntity eventLog  JOIN eventLog.event.influencers influencer where influencer.id = :influencerId and CAST(eventLog.interestedAt as date) = :dateFilter and (:orgId is null or eventLog.event.organization.id = :orgId)")
    int countInterests(Long influencerId, @DateTimeFormat(pattern="yyyy-MM-dd")Date dateFilter, Long orgId);
   @Query("select COUNT(*) from EventLogsEntity eventLog  JOIN eventLog.event.influencers influencer where influencer.id = :influencerId and CAST(eventLog.attendAt as date) = :dateFilter and (:orgId is null or eventLog.event.organization.id = :orgId)")
int countAttends(Long influencerId, @DateTimeFormat(pattern="yyyy-MM-dd")Date dateFilter, Long orgId);


    @Query("SELECT COUNT(DISTINCT eventLog) FROM EventLogsEntity eventLog left JOIN eventLog.event event WHERE event.id = :eventId")
    int countByEventId(@Param("eventId") Long eventId);


    @Query("SELECT Distinct NEW com.nasnav.dto.InterestEventInfo(e.startsAt ,e.name , e.id ,o.id ,o.name," +
            "COALESCE(u.id, emp.id), COALESCE(u.name, emp.name), COALESCE(u.email, emp.email)) " +
            "FROM EventLogsEntity el " +
            "JOIN el.event e " +
            "JOIN e.organization o " +
            "LEFT JOIN el.user u " +
            "LEFT JOIN el.employee emp " +
            "Where  e.startsAt >= :todayStart " +
            "AND e.startsAt < :tomorrowStart")
    PageImpl<InterestEventInfo> findOrganizationUserDTOByIdAndEventStartsToday(@Param("todayStart") LocalDateTime todayStart, @Param("tomorrowStart") LocalDateTime tomorrowStart , Pageable page);

}
