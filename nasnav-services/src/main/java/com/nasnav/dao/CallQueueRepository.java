package com.nasnav.dao;

import com.nasnav.persistence.CallQueueEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CallQueueRepository extends JpaRepository<CallQueueEntity,Long> {
    CallQueueEntity getByUser_IdAndStatus(Long userId, Integer status);
    List<CallQueueEntity> getAllByOrganization_IdAndStatus(Long orgId, Integer status);
    @Query("select queue from CallQueueEntity queue where (:status is null or queue.status=:status)")
    PageImpl<CallQueueEntity> getLogs(Integer status, Pageable page);
}
