package com.nasnav.dao;

import com.nasnav.dto.EventProjection;
import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.OrganizationEntity;
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
    @Query("select event from EventEntity event where event.organization.id =:orgId and (:status is null or event.status =:status)")
    PageImpl<EventEntity> getAllEventForOrg(Long orgId, Integer status, Pageable page);
    @Query("select event from EventEntity event where event.organization.id =:orgId and event.visible = true and (:status is null or event.status =:status)")
    List<EventEntity> getAllEventForUser(Long orgId, Integer status);
    @Query("select event from EventEntity event where (CAST(:dateFilter as date) is null or CAST(event.startsAt as date) = :dateFilter) order by event.startsAt desc")
    PageImpl<EventEntity> getAllEventFilterByDatePageable(@DateTimeFormat(pattern="yyyy-MM-dd")Date dateFilter, Pageable page);
    List<EventEntity> getAllByInfluencerNullAndStartsAtAfter(LocalDateTime now);

//    @Query("SELECT e FROM EventEntity e WHERE (:organization IS NULL OR e.organization = :organization)")
    @Query("SELECT e FROM EventEntity e WHERE (:organization IS NULL OR e.organization = :organization) AND (e.influencer IS NULL )")
    PageImpl<EventProjection> getAllByOrganizationOrFindAll(Pageable page ,@Param("organization") OrganizationEntity organization);

    List<EventEntity> getAllByOrganizationInAndInfluencerNullAndStartsAtAfter(List<OrganizationEntity> orgs, LocalDateTime now);
    @Query("select event from EventEntity event where event.influencer.id =:influencerId and (:orgId is null or event.organization.id =:orgId)")
    PageImpl<EventEntity> getAllByInfluencer_Id(Long influencerId, Long orgId, Pageable page);
    Integer countAllByInfluencer_Id(Long influencerId);
    @Query(value = "select distinct(e.id),e.created_at,e.ends_at,e.starts_at,e.organization_id,e.influencer_id,e.name,e.description,e.status,e.visible from events e" +
            " inner join event_products ep on ep.event_id = e.id inner join products p on p.id = ep.product_id " +
            "where p.category_id in (:categories) and e.visible=true" +
            " and e.status=0 and e.influencer_id is not null" +
            " and e.id != :sourceEventId", nativeQuery = true)
    List<EventEntity> getRelatedEvents(@Param("categories") List<Long> categories, @Param("sourceEventId") Long sourceEventId);
    @Query("select distinct event.organization from EventEntity event where event.influencer.id = :influencerId")
    List<OrganizationEntity> getOrgsThatInfluencerHostFor(Long influencerId);



    @Query( "SELECT event FROM EventEntity event WHERE " +
            "event.influencer IS NOT NULL " +
            "ORDER BY event.startsAt DESC")
    PageImpl<EventProjection> findAllOrderedByStartsAtDesc( Pageable pageable);



    @Query( "SELECT event FROM EventEntity event WHERE " +
            "event.influencer IS NOT NULL " +
            "AND " +
            "( to_timestamp(cast (:startsAt as text), 'yyyy-MM-dd HH24:MI:SS') " +
            "IS NULL OR " +
            "event.startsAt >= " +
            "to_timestamp(cast (:startsAt as text), 'yyyy-MM-dd HH24:MI:SS') )" +
            "ORDER BY event.startsAt DESC")
    PageImpl<EventProjection> findAllByStartOrderedByStartsAtDesc(@Param("startsAt") LocalDateTime startsAt, Pageable pageable);



}

