package com.nasnav.persistence;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.ReturnRequestItemDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name="return_request_item")
@Data
@NoArgsConstructor
public class ReturnRequestItemEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="return_request_id")
    private ReturnRequestEntity returnRequest;

    @ManyToOne
    @JoinColumn(name="return_shipment_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReturnShipmentEntity returnShipment;

    @OneToOne
    @JoinColumn(name="order_item_id")
    private BasketsEntity basket;

    @Column(name = "returned_quantity")
    private Integer returnedQuantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @ManyToOne
    @JoinColumn(name="received_by")
    private EmployeeUserEntity receivedBy;

    @Column(name="received_on")
    private LocalDateTime receivedOn;

    @ManyToOne
    @JoinColumn(name="created_by_user")
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name="created_by_employee")
    private EmployeeUserEntity createdByEmployee;


    public ReturnRequestItemEntity(Long id){
        this.id = id;
    }


    @Override
    public BaseRepresentationObject getRepresentation() {
        ReturnRequestItemDTO dto = new ReturnRequestItemDTO();

        dto.setId(getId());
        if (getReceivedBy() != null) {
            dto.setReceivedBy(getReceivedBy().getId());
        }
        if (getCreatedByUser() != null) {
            dto.setCreatedByUser(getCreatedByUser().getId());
        }
        if (getCreatedByEmployee() != null) {
            dto.setCreatedByEmployee(getCreatedByEmployee().getId());
        }
        if (getBasket() != null) {
            ProductVariantsEntity variant = getBasket().getStocksEntity().getProductVariantsEntity();
            String productName = variant.getProductEntity().getName();
            dto.setBasketItem(getBasket().getId());
            dto.setVariantId(variant.getId());
            dto.setProductName(productName);
        }

        dto.setReceivedQuantity(getReceivedQuantity());
        dto.setReturnedQuantity(getReturnedQuantity());
        dto.setReceivedOn(getReceivedOn());

        return dto;
    }
}
