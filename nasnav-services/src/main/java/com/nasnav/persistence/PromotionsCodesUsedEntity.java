package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;
import java.time.LocalDateTime;

import static javax.persistence.GenerationType.IDENTITY;

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
	private LocalDateTime time;
}
