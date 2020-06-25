package com.nasnav.persistence;

import static com.nasnav.enumerations.PaymentStatus.UNPAID;
import static java.time.LocalDateTime.now;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Type;
import org.hibernate.annotations.UpdateTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrderRepresentationObject;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.persistence.listeners.OrdersEntityListener;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Entity
@Table(name="orders")
@EntityListeners(OrdersEntityListener.class)
@Data
@EqualsAndHashCode(callSuper=false)
public class OrdersEntity implements BaseEntity{

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "id", unique = true, nullable = false)
	private Long id;

	@Column(name = "email", length = 40)
	private String email;

	@Column(name = "name", length = 40)
	private String name;

	@Column(name = "user_id", nullable = false)
	private Long userId;

	@Column(name = "payment_type")
	private Integer payment_type;

	@Column(name = "payment_status", nullable = false)
	private Integer paymentStatus;

	public PaymentStatus getPaymentStatus() {
		return PaymentStatus.getPaymentStatus(paymentStatus);
	}
	
	public void setPaymentStatus(PaymentStatus paymentStatus) {
		this.paymentStatus = paymentStatus.getValue();
	}
	
	public void setOrderStatus(OrderStatus status) {
		this.status = status.getValue();
	}
	
	public OrderStatus getOrderStatus() {
		return OrderStatus.findEnum(status);
	}

	@Column(name = "created_at", nullable = false, length = 29)
	@CreationTimestamp
	private LocalDateTime creationDate;

	@Column(name = "updated_at", nullable = false, length = 29)
	@UpdateTimestamp
	private LocalDateTime updateDate;

	@Column(name = "date_delivery", nullable = false, length = 29)
	private LocalDateTime deliveryDate;

	private Integer status;

    @Type(type = "com.nasnav.persistence.GenericArrayType")
	private String []cancelation_reasons;
	private String driver_name;
	private Boolean equipped;
	private BigDecimal amount;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id", referencedColumnName = "id")
	@JsonIgnore
	private AddressesEntity addressEntity;

	//TODO decide between relational or not
	//@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	private ShopsEntity shopsEntity;

	//TODO decide if deprecated or not
	@JsonIgnore
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", nullable = false)
	private OrganizationEntity organizationEntity;

	@OneToMany(mappedBy = "ordersEntity", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
    private Set<BasketsEntity> basketsEntity;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "payment_id", referencedColumnName = "id")
	private PaymentEntity paymentEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meta_order_id", referencedColumnName = "id")
	private MetaOrderEntity metaOrder;

	@OneToOne(mappedBy="subOrder")
	private ShipmentEntity shipment;

	@Override
	public BaseRepresentationObject getRepresentation() {
		OrderRepresentationObject orderRepresentationObject = new OrderRepresentationObject();
		orderRepresentationObject.setId(getId());
		if (getUserId() != null){
			orderRepresentationObject.setUserId(getUserId());
		}
		if (shopsEntity != null) {
			orderRepresentationObject.setShopId(shopsEntity.getId());
		}
		orderRepresentationObject.setStatus(OrderStatus.findEnum(getStatus()).name());
		return orderRepresentationObject;
	}

	public OrdersEntity() {
		this.paymentStatus = UNPAID.getValue();
		this.creationDate = now();
		basketsEntity = new HashSet<>();
	}
	
	
	
	
	
	public void addBasketItem(BasketsEntity item) {
		item.setOrdersEntity(this);
		basketsEntity.add(item);
	}

	
	
	public void removeBasketItem(BasketsEntity item) {
		item.setOrdersEntity(null);
		basketsEntity.remove(item);
	}
}