package com.nasnav.dao;

import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EmployeeUserHeartBeatsLogsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeUserHeartBeatsLogsRepository extends JpaRepository<EmployeeUserHeartBeatsLogsEntity, Long> {
    @Query("select euhble.employeeUserEntity from EmployeeUserHeartBeatsLogsEntity euhble where euhble.employeeUserEntity.organizationId= :orgId and euhble.creationDate> :time")
    List<EmployeeUserEntity> findByOrganizationIdAndUserStatus(@Param("orgId") Long orgId, @Param("time") LocalDateTime time, Pageable pageable);

    @Modifying
    @Query(value = "insert into employee_user_heart_beats_logs (created_at, employee_user_id) values (:time,:employeeId) on conflict (employee_user_id) do update set created_at= :time", nativeQuery = true)
    void save(@Param("time") LocalDateTime time,@Param("employeeId") Long employeeId);
}
