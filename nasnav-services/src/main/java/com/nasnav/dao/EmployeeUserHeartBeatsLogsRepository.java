package com.nasnav.dao;

import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.EmployeeUserHeartBeatsLogsEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface EmployeeUserHeartBeatsLogsRepository extends JpaRepository<EmployeeUserHeartBeatsLogsEntity, Long> {
    @Query("select distinct euhble.employeeUserEntity from EmployeeUserHeartBeatsLogsEntity euhble where euhble.employeeUserEntity.organizationId= :orgId and euhble.creationDate> :time")
    List<EmployeeUserEntity> findByOrganizationIdAndUserStatus(@Param("orgId") Long orgId, @Param("time") LocalDateTime time, Pageable pageable);
}
