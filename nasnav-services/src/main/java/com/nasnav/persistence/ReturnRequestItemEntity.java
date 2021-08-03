package com.nasnav.persistence;

import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.ReturnRequestItemDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

import static java.util.stream.Collectors.toMap;

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
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReturnRequestEntity returnRequest;

    @ManyToOne
    @JoinColumn(name="return_shipment_id", referencedColumnName = "id")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private ReturnShipmentEntity returnShipment;

    @ManyToOne
    @JoinColumn(name="order_item_id")
    private BasketsEntity basket;

    @Column(name = "returned_quantity")
    private Integer returnedQuantity;

    @Column(name = "received_quantity")
    private Integer receivedQuantity;

    @ManyToOne
    @JoinColumn(name="received_by")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private EmployeeUserEntity receivedBy;

    @Column(name="received_on")
    private LocalDateTime receivedOn;

    @ManyToOne
    @JoinColumn(name="created_by_user")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
    private UserEntity createdByUser;

    @ManyToOne
    @JoinColumn(name="created_by_employee")
    @EqualsAndHashCode.Exclude
    @ToString.Exclude
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
            BasketsEntity basket = getBasket();
            OrdersEntity subOrder = basket.getOrdersEntity();
            StocksEntity stock = basket.getStocksEntity();
            ShopsEntity shop = stock.getShopsEntity();
            ProductVariantsEntity variant = stock.getProductVariantsEntity();
            ProductEntity product = variant.getProductEntity();
            AddressRepObj address = (AddressRepObj)subOrder.getAddressEntity().getRepresentation();
            BigDecimal totalPrice = basket.getPrice().multiply( new BigDecimal(getReturnedQuantity()));
            dto.setAddress(address);
            dto.setBasketItem(getBasket().getId());
            dto.setShopId(shop.getId());
            dto.setShopName(shop.getName());
            dto.setPrice(totalPrice);
            dto.setVariantId(variant.getId());
            dto.setProductName(product.getName());
            dto.setProductId(product.getId());
            dto.setProductCode(variant.getProductCode());
            dto.setSku(variant.getSku());
            dto.setSubOrderId(subOrder.getId());
            dto.setVariantFeatures(
                    variant
                    .getFeatureValues()
                    .stream()
                    .collect(toMap(f -> f.getFeature().getName(), VariantFeatureValueEntity::getValue)));
        }

        dto.setReceivedQuantity(getReceivedQuantity());
        dto.setReturnedQuantity(getReturnedQuantity());
        dto.setReceivedOn(getReceivedOn());

        return dto;
    }
}
