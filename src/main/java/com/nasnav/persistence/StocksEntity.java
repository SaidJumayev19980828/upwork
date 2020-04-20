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
import com.nasnav.enumerations.TransactionCurrency;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

@Table(name="stocks")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class StocksEntity {

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private Integer quantity;
	private String location;

	@Column(name = "price")
	private BigDecimal price;

	@Column(name = "currency")
	private int currency;

	private BigDecimal discount;

	
	@ManyToOne
	@JoinColumn(name = "organization_id", nullable = false)
	@JsonIgnore
	private OrganizationEntity organizationEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	@JsonIgnore
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private ShopsEntity shopsEntity;

	

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name="variant_id")
	@JsonIgnore
	private ProductVariantsEntity productVariantsEntity;
	
	
	public TransactionCurrency getCurrency() {
		return TransactionCurrency.getTransactionCurrency(this.currency);
	}
	
	
	public void setCurrency(TransactionCurrency currency) {
		this.currency = currency.getValue();
	}


}