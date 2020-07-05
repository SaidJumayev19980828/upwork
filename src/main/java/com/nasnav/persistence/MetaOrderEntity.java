package com.nasnav.persistence;

import static com.nasnav.enumerations.OrderStatus.CLIENT_CONFIRMED;
import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;
import static javax.persistence.CascadeType.REMOVE;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.CreationTimestamp;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.EqualsAndHashCode.Exclude;
import lombok.ToString;

@Table(name = "meta_orders")
@Data
@Entity
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

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "organization_id", nullable = false)
    private OrganizationEntity organization;

    @OneToMany(mappedBy = "metaOrder", cascade = {PERSIST, MERGE, REMOVE})
    @Exclude
    @ToString.Exclude
    private Set<OrdersEntity> subOrders;

    @Override
    public BaseRepresentationObject getRepresentation() {
        return new BaseRepresentationObject();
    }

    public MetaOrderEntity() {
        subOrders = new HashSet<>();
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
}
