package com.nasnav.persistence;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.ReturnRequestDTO;
import com.nasnav.enumerations.ReturnRequestStatus;
import com.nasnav.shipping.model.Shipment;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import static java.util.Optional.ofNullable;
import static javax.persistence.CascadeType.ALL;

@Entity
@Table(name="return_request")
@Data
public class ReturnRequestEntity implements BaseEntity{

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Column(name="created_on")
    @CreationTimestamp
    private LocalDateTime createdOn;

    @ManyToOne
    @JoinColumn(name="created_by_user")
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name="created_by_employee")
    private EmployeeUserEntity createdByEmployee;

    @ManyToOne
    @JoinColumn(name="meta_order_id")
    private MetaOrderEntity metaOrder;

    @Column(name = "status")
    private Integer status;

    @OneToMany(mappedBy = "returnRequest", cascade = ALL)
    @JsonIgnore
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    private Set<ReturnRequestItemEntity> returnedItems;


    public ReturnRequestEntity() {
        returnedItems = new HashSet<>();
    }



    public void addItem(ReturnRequestItemEntity item){
        item.setReturnRequest(this);
        this.returnedItems.add(item);
    }

    @Override
    public BaseRepresentationObject getRepresentation() {
        ReturnRequestDTO dto = new ReturnRequestDTO();

        dto.setId(getId());
        dto.setCreatedOn(getCreatedOn());
        dto.setStatus(ReturnRequestStatus.findEnum(getStatus()));

        if (getMetaOrder() != null) {
            MetaOrderEntity metaOrder = getMetaOrder();
            String addressPhoneNumber = "";
            OrdersEntity subOrder = getFirstSubOrder(metaOrder);
            String shippingServiceId = getFirstReturnShipmentShippingServiceId();
            if (subOrder != null) {
                addressPhoneNumber = ofNullable(subOrder
                                                .getAddressEntity()
                                                .getPhoneNumber())
                                    .orElse("");
            }
            dto.setMetaOrderId(metaOrder.getId());
            dto.setPhoneNumber(addressPhoneNumber);
            dto.setShippingServiceId(shippingServiceId);
        }
        if (getCreatedByEmployee() != null) {
            dto.setCreatedByEmployee(getCreatedByEmployee().getId());
        }
        if (getCreatedByUser() != null) {
            dto.setCreatedByUser(getCreatedByUser().getId());
            dto.setUserName(getCreatedByUser().getName());
        }
        dto.setItemsCount(new Long(returnedItems.size()));
        return dto;
    }




    private String getFirstReturnShipmentShippingServiceId() {
        return returnedItems
                .stream()
                .findFirst()
                .map(ReturnRequestItemEntity::getReturnShipment)
                .map(ReturnShipmentEntity::getShippingServiceId)
                .orElse(null);
    }



    private OrdersEntity getFirstSubOrder(MetaOrderEntity metaOrder) {
        return metaOrder
                .getSubOrders()
                .stream()
                .findFirst()
                .get();
    }
}
