package com.nasnav.persistence;

import static com.nasnav.enumerations.EventRoomStatus.*;

import java.time.LocalDateTime;
import java.util.Set;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.PreRemove;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.cloud.firestore.annotation.Exclude;
import com.nasnav.enumerations.EventRoomStatus;
import com.nasnav.enumerations.RoomSessionStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@DiscriminatorValue("EVENT")
@Data
@EqualsAndHashCode(callSuper = false)

public class EventRoomTemplateEntity extends RoomTemplateEntity {
	private static final Set<EventRoomStatus> STARTABLE_STATES = Set.of(NOT_STARTED, STARTED, SUSPENDED);
		private static final Set<EventRoomStatus> SUSPENDABLE_STATES = Set.of(STARTED, SUSPENDED);

	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name = "event_id", referencedColumnName = "id")
	@JsonIgnore
	@Exclude
	@lombok.ToString.Exclude
	EventEntity event;
	public EventRoomStatus getStatus() {
		if (event.getEndsAt().isBefore(LocalDateTime.now())) {
			return EventRoomStatus.ENDED;
		}

		if (session != null) {
			if (session.getStatus() == RoomSessionStatus.STARTED) {
				return STARTED;
			}

			if (session.getStatus() == RoomSessionStatus.SUSPENDED) {
				return EventRoomStatus.SUSPENDED;
			}
		}

		return EventRoomStatus.NOT_STARTED;
	}

	@Override
	public void start(String sessionExternalId) {
		if (!STARTABLE_STATES.contains(getStatus())) {
			throw new IllegalStateException();
		}
		super.start(sessionExternalId);
	}

	public void suspend() {
		if (!SUSPENDABLE_STATES.contains(getStatus())) {
			throw new IllegalStateException();
		}
		session.suspend();
	}
}
