package com.nasnav.persistence;

import lombok.Data;

import javax.persistence.*;

import static javax.persistence.GenerationType.IDENTITY;

@Entity
@Table(name="PROMOTIONS_CART_CODES")
@Data
public class PromotionsCartCodes {
	@Id
    @GeneratedValue(strategy=IDENTITY)
	private Long id;
	
	@ManyToOne
	@JoinColumn(name="user_id")
	private UserEntity user;
	
	@Column(name="code")
	private String code;
}
