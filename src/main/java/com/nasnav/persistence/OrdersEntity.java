package com.nasnav.persistence;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.OrderRepresentationObject;
import com.nasnav.enumerations.OrderStatus;
import com.nasnav.enumerations.PaymentStatus;
import com.nasnav.persistence.listeners.OrdersEntityListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.nasnav.enumerations.PaymentStatus.UNPAID;
import static java.time.LocalDateTime.now;

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

	@Column(name = "name", length = 40)
	private String name;

	@Column(name = "user_id", nullable = false)
	private Long userId;

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

	private BigDecimal amount;

	@OneToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "address_id", referencedColumnName = "id")
	@Exclude
	@ToString.Exclude
	private AddressesEntity addressEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "shop_id", nullable = false)
	@Exclude
	@ToString.Exclude
	private ShopsEntity shopsEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "organization_id", nullable = false)
	@Exclude
	@ToString.Exclude
	private OrganizationEntity organizationEntity;

	@OneToMany(mappedBy = "ordersEntity", cascade = {CascadeType.PERSIST, CascadeType.REMOVE})
	@Exclude
	@ToString.Exclude
    private Set<BasketsEntity> basketsEntity;

	@ManyToOne(cascade = CascadeType.MERGE)
	@JoinColumn(name = "payment_id", referencedColumnName = "id")
	@Exclude
	@ToString.Exclude
	private PaymentEntity paymentEntity;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "meta_order_id", referencedColumnName = "id")
	@Exclude
	@ToString.Exclude
	private MetaOrderEntity metaOrder;

	@OneToMany(mappedBy="subOrder")
	@Exclude
    @ToString.Exclude
	private List<ShipmentEntity> shipment;

	
	private BigDecimal total;
	
	private BigDecimal discounts;
	
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