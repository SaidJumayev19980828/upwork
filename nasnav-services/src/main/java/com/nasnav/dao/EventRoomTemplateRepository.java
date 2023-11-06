package com.nasnav.dao;

import java.time.LocalDateTime;
import java.util.Optional;

import javax.persistence.criteria.Expression;

import com.nasnav.dto.EventRoomProjection;
import com.nasnav.persistence.OrganizationEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import com.nasnav.enumerations.EventRoomStatus;
import com.nasnav.enumerations.RoomSessionStatus;
import com.nasnav.persistence.EventRoomTemplateEntity;
import org.springframework.data.repository.query.Param;

public interface EventRoomTemplateRepository
		extends JpaRepository<EventRoomTemplateEntity, Long>, JpaSpecificationExecutor<EventRoomTemplateEntity> {
	Optional<EventRoomTemplateEntity> findByEventId(Long eventId);

	int deleteTemplateByEventIdAndEventOrganizationId(Long eventId, Long orgId);

	Page<EventRoomTemplateEntity> findAllByEventOrganizationId(Long orgId, Pageable pageable);

	default Page<EventRoomTemplateEntity> findAllByStatus(EventRoomStatus status,
			Specification<EventRoomTemplateEntity> additionalSpec,
			Pageable pageable) {
		return findAll((root, criteriaQuery, criteriaBuilder) -> {
			Expression<?> column = criteriaBuilder.selectCase()
					.when(criteriaBuilder.lessThan(root
							.get("event")
							.get("endsAt"), LocalDateTime.now()), EventRoomStatus.ENDED)
					.when(criteriaBuilder.equal(root
							.get("session")
							.get("status"), RoomSessionStatus.STARTED), EventRoomStatus.STARTED)
					.when(criteriaBuilder.equal(root
							.get("session")
							.get("status"), RoomSessionStatus.SUSPENDED), EventRoomStatus.SUSPENDED)
					.otherwise(EventRoomStatus.NOT_STARTED);
			return criteriaBuilder.and(criteriaBuilder.equal(column, status),
					additionalSpec.toPredicate(root, criteriaQuery, criteriaBuilder));
		}, pageable);
	}

	public default Page<EventRoomTemplateEntity> findAllByEventOrganizationIdAndStatus(Long orgId,
			EventRoomStatus status,
			Pageable pageable) {
		return findAllByStatus(status, (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("event")
						.get("organization")
						.get("id"), orgId),
				pageable);
	}

	@Query("select rt from EventRoomTemplateEntity rt join rt.event e join e.organization o where o.yeshteryState = 1")
	Page<EventRoomTemplateEntity> findAllByEventOrganizationYeshteryStateEquals1(Pageable pageable);


	@Query("" +
			"select template  as template,   event as event  , count(DISTINCT el) as interest " +
			" from EventRoomTemplateEntity template " +
			" Left JOIN template.event event" +
			" Left JOIN event.organization o " +
			" Left JOIN event.influencers influencer "+
			" LEFT JOIN EventLogsEntity el ON el.event = event.id " +
			" where  (:organization is null or event.organization= :organization)"+
			"GROUP BY template, event " +
          	"ORDER BY event.startsAt DESC"
	)
	PageImpl<EventRoomProjection> findAllByEventOrganization(@Param("organization") OrganizationEntity organization, Pageable pageable);

	public default Page<EventRoomTemplateEntity> findAllByEventOrganizationYeshteryStateEquals1AndStatus(
			EventRoomStatus status, Pageable pageable) {
		return findAllByStatus(status, (root, criteriaQuery, criteriaBuilder) -> criteriaBuilder
				.equal(root.get("event")
						.get("organization")
						.get("yeshtery_state"), 1),
				pageable);
	}



}
