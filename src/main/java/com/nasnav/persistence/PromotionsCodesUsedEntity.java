package com.nasnav.persistence;

import static javax.persistence.GenerationType.IDENTITY;

import java.time.LocalDateTime;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import lombok.Data;

@Entity
@Table(name="PROMOTIONS_CODES_USED")
@Data
public class PromotionsCodesUsedEntity {
	@Id
    @GeneratedValue(strategy=IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="promotion_id")
	private PromotionsEntity promotion;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserEntity user;
	
	@Column(name="time")
	@CreationTimestamp
	private LocalDateTime time;
}
