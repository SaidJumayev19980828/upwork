package com.nasnav.persistence;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.hibernate.annotations.Type;
import org.springframework.data.jpa.domain.AbstractPersistable;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="orders")
@Data
@EqualsAndHashCode(callSuper=false)
public class OrdersEntity extends AbstractPersistable<Long> implements BaseEntity{

	@Id
	@Column(name = "id", unique = true, nullable = false)
	private Long id;
	@Column(name = "address", length = 150)
	private String address;
	@Column(name = "email", length = 40)
	private String email;
	@Column(name = "name", length = 40)
	private String name;
	@Column(name = "payment_type")
	private Integer payment_type;
	private Long user_id;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "created_at", nullable = false, length = 29)
	private Date creationDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "updated_at", nullable = false, length = 29)
	private Date updateDate;
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "date_delivery", nullable = false, length = 29)
	private Date deliveryDate;
	private Integer status;
    @Type(type = "com.nasnav.persistence.GenericArrayType")
	private String []cancelation_reasons;
	private String driver_name;
	private Boolean equipped;
	private BigDecimal amount;


//	private Integer currency;


//	@Column(name ="shop_id")
//	private Long shopId;
	//TODO decide between relational or not
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private ShopsEntity shopsEntity;

	//TODO decide if deprecated or not
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationEntity organizationEntity;

    @OneToOne(mappedBy = "ordersEntity")
    private BasketsEntity basketsEntity;

	@Override
	public BaseRepresentationObject getRepresentation() {
		// TODO Auto-generated method stub
		return null;
	}
}