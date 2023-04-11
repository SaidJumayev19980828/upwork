package com.nasnav.persistence;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import com.fasterxml.jackson.annotation.JsonIgnore;

import lombok.Data;

@Table(name = "addon_basket")
@Entity
@Data
public class AddonBasketEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(columnDefinition = "serial")
	private Long id;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "addon_stock_id", referencedColumnName = "id")
	@JsonIgnore
	private AddonStocksEntity stocksEntity;
	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "basket_id", referencedColumnName = "id")
	@JsonIgnore
	private BasketsEntity basketEntity;

	@Column(name = "price", precision = 10, scale = 2)
	private BigDecimal price;

}
