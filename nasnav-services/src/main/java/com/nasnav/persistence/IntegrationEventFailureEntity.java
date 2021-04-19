package com.nasnav.persistence;

import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "INTEGRATION_EVENT_FAILURE")
public class IntegrationEventFailureEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="organization_id")
	private Long organizationId;
	
	@Column(name="event_type")
	private String eventType;
	
	@Column(name="event_data")
	private String eventData;
	
	@Column(name="created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@Column(name="handle_exception")
	private String handleException;
	
	
	@Column(name="fallback_exception")
	private String fallbackException;
}
