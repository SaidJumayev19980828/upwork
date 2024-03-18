package com.nasnav.dao;

import com.nasnav.dto.PersonalEvent;
import com.nasnav.persistence.EmployeeUserEntity;
import com.nasnav.persistence.PersonalEventEntity;
import com.nasnav.persistence.UserEntity;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PersonalEventEntityRepository extends JpaRepository<PersonalEventEntity, Long> {

    @Query("""
        SELECT pe FROM PersonalEventEntity pe
        LEFT JOIN FETCH pe.user u
        LEFT JOIN FETCH pe.employee e
        WHERE pe.startsAt > :now
        AND pe.canceled = false
        AND pe.id = :id AND (u = :user OR e = :employee)
        """)
    Optional<PersonalEventEntity> findByIdAndStartsAtAfter(Long id , LocalDateTime now, UserEntity user , EmployeeUserEntity employee);

    Optional<PersonalEvent> findAllById(Long id);

    PageImpl<PersonalEvent> findAllBy(Pageable page);

    @Query("""
    SELECT pe.id AS id, pe.name AS name, pe.startsAt AS startsAt, pe.endsAt AS endsAt,
        CASE
            WHEN pe.user = :user OR pe.employee = :employee THEN true
            ELSE false
        END AS isEventCreator,
        CASE
            WHEN ei.user = :user OR ei.employee = :employee THEN true
            ELSE false
        END AS isInvitedToEvent
    FROM PersonalEventEntity pe
    LEFT JOIN pe.invitees ei
    WHERE
        (pe.user = :user OR pe.employee = :employee)
        OR (ei.user = :user OR ei.employee = :employee)
""")
    List<Map<String, Object>> findMyAllEvents(@Param("user") UserEntity user, @Param("employee") EmployeeUserEntity employee);
}
