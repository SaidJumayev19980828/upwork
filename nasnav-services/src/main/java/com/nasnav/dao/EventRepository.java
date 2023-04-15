package com.nasnav.dao;

import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.InfluencerEntity;
import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface EventRepository extends CrudRepository<EventEntity, Long> {
    @Query("select event from EventEntity event where event.organization.id =:orgId and (:status is null or event.status =:status)")
    PageImpl<EventEntity> getAllEventForOrg(Long orgId, Integer status, Pageable page);
    @Query("select event from EventEntity event where event.organization.id =:orgId and event.visible = true and (:status is null or event.status =:status)")
    List<EventEntity> getAllEventForUser(Long orgId, Integer status);
    List<EventEntity> getAllByInfluencerNull();
    List<EventEntity> getAllByOrganizationInAndInfluencerNull(List<OrganizationEntity> orgs);
    @Query("select event from EventEntity event where event.influencer.id =:influencerId and (:orgId is null or event.organization.id =:orgId)")
    PageImpl<EventEntity> getAllByInfluencer_Id(Long influencerId, Long orgId, Pageable page);
    Integer countAllByInfluencer_Id(Long influencerId);
    @Query(value = "select distinct(e.id),e.created_at,e.ends_at,e.starts_at,e.organization_id,e.influencer_id,e.name,e.description,e.status,e.visible from events e" +
            " inner join event_products ep on ep.event_id = e.id inner join products p on p.id = ep.product_id " +
            "where p.category_id in (:categories) and e.visible=true" +
            " and e.status=0 and e.influencer_id is not null" +
            " and e.id != :sourceEventId", nativeQuery = true)
    List<EventEntity> getRelatedEvents(@Param("categories") List<Long> categories, @Param("sourceEventId") Long sourceEventId);
}
