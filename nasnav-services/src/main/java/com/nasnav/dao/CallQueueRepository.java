package com.nasnav.dao;

import com.nasnav.persistence.CallQueueEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CallQueueRepository extends JpaRepository<CallQueueEntity,Long> {
    CallQueueEntity getByUser_IdAndStatus(Long userId, Integer status);
    List<CallQueueEntity> getAllByOrganization_IdAndStatus(Long orgId, Integer status);
    @Query("select queue from CallQueueEntity queue where (:status is null or queue.status=:status)")
    PageImpl<CallQueueEntity> getLogs(Integer status, Pageable page);

    @Query(value = "select * from call_queue q where q.employee_id is null and q.status = 0 and now() < q.joins_at", nativeQuery = true)
    List<CallQueueEntity> findScheduledCalls();
    @Query(value = "select * from call_queue q where q.employee_id = :empId and q.status = 0 and now() < q.joins_at", nativeQuery = true)
    List<CallQueueEntity> findUpcomingCalls(@Param("empId") Long empId);
    @Query(value = "select * from call_queue q where (current_date <= q.joins_at) and (q.joins_at <= current_date + INTERVAL '1 day') ", nativeQuery = true)
    List<CallQueueEntity> findTodayTotalCalls();
}
