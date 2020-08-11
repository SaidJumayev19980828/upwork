package com.nasnav.persistence;

import static javax.persistence.GenerationType.IDENTITY;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

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
