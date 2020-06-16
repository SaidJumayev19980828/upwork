package com.nasnav.persistence;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import lombok.Data;

@Table(name = "CART_ITEMS")
@Entity
@Data
public class CartItemEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@Column(name="COVER_IMAGE")
	private String coverImage;
	
	@Column(name="VARIANT_FEATURES")
	private String variantFeatures;
	
	private Integer quantity;
	
	@ManyToOne
	@JoinColumn(name="STOCK_ID", referencedColumnName = "ID")
	private StocksEntity stock;
	
	@ManyToOne
	@JoinColumn(name="USER_ID")
	private UserEntity user;
}
