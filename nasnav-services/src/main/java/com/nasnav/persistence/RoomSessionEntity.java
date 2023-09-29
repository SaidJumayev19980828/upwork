package com.nasnav.persistence;

import static com.nasnav.commons.utils.StringUtils.isNotBlankOrNull;

import java.time.LocalDateTime;
import java.util.UUID;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EntityListeners;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import com.nasnav.enumerations.RoomSessionStatus;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@Entity
@Table(name = "room_sessions")
@EqualsAndHashCode(callSuper=false)
@EntityListeners({AuditingEntityListener.class})
public class RoomSessionEntity extends DefaultBusinessEntity<Long> {
	@OneToOne(fetch = FetchType.EAGER, optional = false)
	@JoinColumn(name =  "template_id", referencedColumnName = "id")
	private RoomTemplateEntity template;

	@Enumerated(EnumType.STRING)
	@Column(name = "status")
	private RoomSessionStatus status = RoomSessionStatus.STARTED;

	// unique?
	@Column(name = "external_id", nullable = false)
	private String externalId;


	@Column(name = "created_at")
	@CreatedDate
	private LocalDateTime createdAt;

	// naming due to expecting another employee_creator column
	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "user_creator")
	@CreatedBy
	private UserEntity userCreator;

	public RoomSessionEntity(String externalId) {
		start(externalId);
	}

	public void start(String newExternalId) {
		if (isNotBlankOrNull(newExternalId)) {
			setExternalId(newExternalId);
		} else if(externalId == null) {
			setExternalId(UUID.randomUUID().toString());
		}
		this.setStatus(RoomSessionStatus.STARTED);
	}

	public void suspend() {
		this.setStatus(RoomSessionStatus.SUSPENDED);
	}
}
