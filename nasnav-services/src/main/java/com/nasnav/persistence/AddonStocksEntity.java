package com.nasnav.persistence;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Table(name="addon_stocks")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class AddonStocksEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	@Column(columnDefinition = "serial")
	private Long id;

	private Integer quantity;

	@Column(name = "price")
	private BigDecimal price;


	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private ShopsEntity shopsEntity;

	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="addon_id")
	@JsonIgnore
	private AddonEntity addonEntity;
	



}