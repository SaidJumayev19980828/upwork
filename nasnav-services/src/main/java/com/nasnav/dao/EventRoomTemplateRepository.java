package com.nasnav.dao;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import com.nasnav.persistence.EventRoomTemplateEntity;

public interface EventRoomTemplateRepository extends JpaRepository<EventRoomTemplateEntity, Long> {
	Optional<EventRoomTemplateEntity> findByEventId(Long eventId);

	int deleteTemplateByEventIdAndEventOrganizationId(Long eventId, Long orgId);

	Page<EventRoomTemplateEntity> findAllByEventOrganizationId(Long orgId, Pageable pageable);

	@Query("select rt from EventRoomTemplateEntity rt join rt.event e join e.organization o join rt.session s"
			+ " where o.id = :orgId and ((true = :started and s is not null) or (false = :started and s is null))")
	Page<EventRoomTemplateEntity> findAllByEventOrganizationIdAndSessionNullEquals(Long orgId, Boolean started,
			Pageable pageable);

	@Query("select rt from EventRoomTemplateEntity rt join rt.event e join e.organization o where o.yeshteryState = 1")
	Page<EventRoomTemplateEntity> findAllByEventOrganizationYeshteryStateEquals1(Pageable pageable);

	@Query("select rt from EventRoomTemplateEntity rt join rt.event e join e.organization o join rt.session s"
			+ " where o.yeshteryState = 1 and ((true = :started and s is not null) or (false = :started and s is null))")
	Page<EventRoomTemplateEntity> findAllByEventOrganizationYeshteryStateEquals1AndStarted(Boolean started, Pageable pageable);
}
