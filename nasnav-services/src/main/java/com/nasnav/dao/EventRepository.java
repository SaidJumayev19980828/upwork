package com.nasnav.dao;

import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.InfluencerEntity;
import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRepository extends CrudRepository<EventEntity, Long> {
    @Query("select event from EventEntity event where event.organization.id =:orgId and (:status is null or event.status =:status)")
    PageImpl<EventEntity> getAllEventForOrg(Long orgId, Integer status, Pageable page);
    @Query("select event from EventEntity event where event.organization.id =:orgId and event.visible = true and (:status is null or event.status =:status)")
    List<EventEntity> getAllEventForUser(Long orgId, Integer status);
    List<EventEntity> getAllByInfluencerNull();
    List<EventEntity> getAllByOrganizationInAndInfluencerNull(List<OrganizationEntity> orgs);
    List<EventEntity> getAllByInfluencer(InfluencerEntity entity);
}
