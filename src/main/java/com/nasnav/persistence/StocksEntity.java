package com.nasnav.persistence;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Table(name="stocks")
@Entity
@Data
@EqualsAndHashCode(callSuper=false)
public class StocksEntity extends AbstractPersistable<Long> implements BaseEntity{

	@Id
	@GeneratedValue(strategy=GenerationType.IDENTITY)
	private Long id;

	private Integer quantity;
	private String location;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false, length = 29)
	private Date creationDate;

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at", nullable = false, length = 29)
	private Date updateDate;

	private BigDecimal price;
	private BigDecimal discount;

	@Column(name = "sub_product_id")
	private Long subProductId;


	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", nullable = false)
	@JsonIgnore
	private OrganizationEntity organizationEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	//@JsonIgnore
	private ShopsEntity shopsEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "product_id", nullable = false)
	@JsonIgnore
	private ProductEntity productEntity;

//    @OneToOne(mappedBy = "ordersEntity")
//    @JsonIgnore
//    private BasketsEntity basketsEntity;

//    @OneToOne(mappedBy = "productEntity")
//    @JsonIgnore
//    private ProductVariants productVariants;


	@Override
	public BaseRepresentationObject getRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}

}