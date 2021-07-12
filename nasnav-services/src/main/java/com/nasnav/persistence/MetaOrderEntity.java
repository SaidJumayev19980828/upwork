package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.persistence.listeners.MetaOrderEntityListener;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static javax.persistence.CascadeType.*;
import static javax.persistence.FetchType.LAZY;

@Table(name = "meta_orders")
@Data
@Entity
@EntityListeners(MetaOrderEntityListener.class)
@EqualsAndHashCode(callSuper=false)
public class MetaOrderEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "created_at")
    @CreationTimestamp
    private LocalDateTime createdAt;
    
    @Column(name = "status")
    private Integer status;
    
    @Column(name = "grand_total")
    private BigDecimal grandTotal;
    
    @Column(name = "sub_total")
    private BigDecimal subTotal;
    
    @Column(name = "shipping_total")
    private BigDecimal shippingTotal;

    private String notes;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @JsonIgnore
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "metaOrder", cascade = {PERSIST, MERGE, REMOVE})
    @Exclude
    @ToString.Exclude
    private Set<OrdersEntity> subOrders;
    
    private BigDecimal discounts;
    
    @ManyToMany(fetch = LAZY)
    @JoinTable(name = "meta_orders_promotions"
    , joinColumns = {@JoinColumn(name="meta_order")}
    , inverseJoinColumns = {@JoinColumn(name="promotion")})
    private Set<PromotionsEntity> promotions;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return new BaseRepresentationObject();
    }

    public MetaOrderEntity() {
        subOrders = new HashSet<>();
        this.promotions = new HashSet<>();
        this.status = CLIENT_CONFIRMED.getValue();
    }

    public void addSubOrder(OrdersEntity subOrder) {
        subOrder.setMetaOrder(this);
        subOrders.add(subOrder);
    }



    public void removeSubOrder(OrdersEntity subOrder) {
        subOrder.setMetaOrder(null);
        subOrders.remove(subOrder);
    }
    
    
    public void addPromotion(PromotionsEntity promotion) {
    	promotions.add(promotion);
    }
    
    
    public void removePromotion(PromotionsEntity promotion) {
    	promotions.remove(promotion);
    }
}
