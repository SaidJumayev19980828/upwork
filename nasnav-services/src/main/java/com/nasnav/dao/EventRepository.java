package com.nasnav.dao;

import com.nasnav.dto.EventInterestsProjection;
import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.InfluencerEntity;
import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;

public interface EventRepository extends CrudRepository<EventEntity, Long>, JpaSpecificationExecutor<EventEntity> {

    @Query("select event from EventEntity event where event.organization.id =:orgId and event.visible = true and (:status is null or event.status =:status)")
    List<EventEntity> getAllEventForUser(Long orgId, Integer status);
    @Query("select event from EventEntity event where (CAST(:dateFilter as date) is null or CAST(event.startsAt as date) = :dateFilter) order by event.startsAt desc")
    PageImpl<EventEntity> getAllEventFilterByDatePageable(@DateTimeFormat(pattern="yyyy-MM-dd")Date dateFilter, Pageable page);

    @Query("SELECT DISTINCT event " +
            "FROM EventEntity event " +
            "LEFT JOIN event.influencers influencer " +
            "WHERE influencer IS NULL " +
            "AND (:name IS NULL OR event.name like %:name%) " +
            "AND (:status IS NULL OR event.status = :status) " +
            "AND ( to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "        to_timestamp(CAST(event.startsAt as text), 'yyyy-MM-dd HH24:MI:SS') >= " +
            "       to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "        ) " +
            "AND ( to_timestamp(CAST(:endDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "    to_timestamp(CAST(event.endsAt as text), 'yyyy-MM-dd HH24:MI:SS') <= " +
            "    to_timestamp(CAST(:endDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "       )" )
    PageImpl<EventEntity> getAllByInfluencersNullAndDateBetweenAndMatchStatusAndNameIfNotNull(
            @Param("status") Integer status,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            String name,
            Pageable pageable
    );

    @Query("SELECT event as event , count(el) as interest FROM EventEntity event " +
            " LEFT JOIN event.influencers influencer" +
            " LEFT JOIN EventLogsEntity el ON el.event = event.id " +
            " WHERE influencer IS NULL And (:organization is null or event.organization=:organization)" +
            " GROUP BY event.id " )

    PageImpl<EventInterestsProjection> getAllByOrganizationAndInfluencersNull(Pageable page , @Param("organization") OrganizationEntity organization);
    List<EventEntity> getAllByOrganizationInAndInfluencersNullAndStartsAtAfter(List<OrganizationEntity> orgs, LocalDateTime now);
    @Query("select DISTINCT event from EventEntity event   JOIN event.influencers influencer where influencer.id =:influencerId and (:orgId is null or event.organization.id =:orgId)")
    PageImpl<EventEntity> getAllByInfluencers(Long influencerId, Long orgId, Pageable page);


    Integer countAllByInfluencersContains(InfluencerEntity influencer);
    @Query(value = "select distinct(e.id),e.created_at,e.ends_at,e.starts_at,e.organization_id, " +
            " influencer.id,e.name,e.description,e.status,e.visible "+
            "from events e " +
            "inner join event_products ep on ep.event_id = e.id " +
            "inner join products p on p.id = ep.product_id " +
            "left join event_influencers influencer on influencer.event_id=e.id " +
            "where p.category_id in (:categories) and e.visible=true" +
            " and e.status=0 and influencer.id is not null" +
            " and e.id != :sourceEventId", nativeQuery = true)
    List<EventEntity> getRelatedEvents(@Param("categories") List<Long> categories, @Param("sourceEventId") Long sourceEventId);


    @Query("select distinct event.organization from EventEntity event join event.influencers influencer where influencer.id = :influencerId")
    List<OrganizationEntity> getOrgsThatInfluencerHostFor(Long influencerId);

    @Query("SELECT DISTINCT event as event, count(el) as interest FROM EventEntity event JOIN event.influencers influencer " +
            "LEFT JOIN EventLogsEntity el ON el.event = event.id " +
            "WHERE influencer IS NOT NULL  And (:organization is null or event.organization=:organization) " +
            "AND (:status IS NULL OR " +
            "(:status = 'RUNNING' AND to_timestamp(CAST(event.startsAt as text), 'yyyy-MM-dd HH24:MI:SS') <= to_timestamp(CAST(:now as text), 'yyyy-MM-dd HH24:MI:SS') AND to_timestamp(CAST(event.endsAt as text), 'yyyy-MM-dd HH24:MI:SS') >= to_timestamp(CAST(:now as text), 'yyyy-MM-dd HH24:MI:SS')) OR " +
            "(:status = 'UPCOMING' AND to_timestamp(CAST(event.startsAt as text), 'yyyy-MM-dd HH24:MI:SS') > to_timestamp(CAST(:now as text), 'yyyy-MM-dd HH24:MI:SS')) OR " +
            "(:status = 'FINISHED' AND to_timestamp(CAST(event.endsAt as text), 'yyyy-MM-dd HH24:MI:SS') < to_timestamp(CAST(:now as text), 'yyyy-MM-dd HH24:MI:SS'))) " +
            "GROUP BY event.id " +
            "ORDER BY event.startsAt DESC")
    Page<EventInterestsProjection> findAllOrderedByStartsAtDesc(Pageable pageable , @Param("organization") OrganizationEntity organization, String status, LocalDateTime now);

    @Query("SELECT DISTINCT event as event, count(el) as interest FROM EventEntity event JOIN event.influencers influencer " +
            "LEFT JOIN EventLogsEntity el ON el.event = event.id " +
            "WHERE influencer IS NOT NULL  And (:organization is null or event.organization=:organization) " +
            "AND (  " +
            "       ( to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "        to_timestamp(CAST(event.startsAt as text), 'yyyy-MM-dd HH24:MI:SS') >= " +
            "       to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "        ) " +
            "       AND" +
            "       ( to_timestamp(CAST(:endDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "    to_timestamp(CAST(event.endsAt as text), 'yyyy-MM-dd HH24:MI:SS') <= " +
            "    to_timestamp(CAST(:endDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "       )" +
            ")" +
            "GROUP BY event.id " +
            "ORDER BY event.startsAt DESC")
    Page<EventInterestsProjection> findAllOrderedByStartsAtDesc(Pageable pageable , @Param("organization") OrganizationEntity organization,
                                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                                    LocalDateTime fromDate,
                                                                    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
                                                                    LocalDateTime endDate
    );

    @Query("SELECT DISTINCT event as event, COUNT(el.id) AS interest " +
            "FROM EventEntity event " +
            "LEFT JOIN EventLogsEntity el ON el.event = event.id " +
            "WHERE event.organization.id = :organizationId " +
            "AND (:name IS NULL OR event.name like %:name%) " +
            "AND (:status IS NULL OR event.status = :status) " +
            "AND (  " +
            "       ( to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "        to_timestamp(CAST(event.startsAt as text), 'yyyy-MM-dd HH24:MI:SS') >= " +
            "       to_timestamp(CAST(:fromDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "        ) " +
            "       AND" +
            "       ( to_timestamp(CAST(:endDate as text), 'yyyy-MM-dd HH24:MI:SS') IS NULL OR " +
            "    to_timestamp(CAST(event.endsAt as text), 'yyyy-MM-dd HH24:MI:SS') <= " +
            "    to_timestamp(CAST(:endDate as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "       )" +
            ")" +
            "GROUP BY event.id")
    PageImpl<EventInterestsProjection> findAllEventsByStatusAndOrganizationIdAndBetweenDateTime(
            @Param("organizationId") Long organizationId,
            @Param("status") Integer status,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate,
            String name,
            Pageable pageable);

    @Query("select event from EventEntity event where event.name =:name")
    PageImpl<EventEntity> findByName(String name,Pageable page);

}

