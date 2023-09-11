package com.nasnav.dao;

import com.nasnav.persistence.EventEntity;
import com.nasnav.persistence.EventRequestsEntity;
import com.nasnav.persistence.InfluencerEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface EventRequestsRepository extends CrudRepository<EventRequestsEntity, Long> {
    boolean existsByInfluencerAndEvent(InfluencerEntity influencerEntity, EventEntity eventEntity);
    List<EventRequestsEntity> getAllByEvent_Organization_Id(Long orgId);
    void deleteAllByEvent_Id(Long eventId);
    @Query("select request from EventRequestsEntity request where request.event.organization.id =:orgId  and (:status IS null or request.status =:status)")
    PageImpl<EventRequestsEntity> getAllByOrgIdPageable(Long orgId, Integer status, Pageable page);
    @Query("select request from EventRequestsEntity request where request.influencer.id =:id and (:status IS null or request.status =:status)")
    PageImpl<EventRequestsEntity> getAllByInfluencerIdPageable(Long id, Integer status, Pageable page);
    boolean existsByEvent_IdAndStatusEquals(Long eventId, int status);
    List<EventRequestsEntity> getAllByInfluencer_Id(Long influencerId);
}
