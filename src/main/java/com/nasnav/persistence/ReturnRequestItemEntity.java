package com.nasnav.persistence;

import com.nasnav.dto.AddressRepObj;
import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.ReturnRequestItemDTO;
import lombok.Data;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name="return_request_item")
@Data
public class ReturnRequestItemEntity implements BaseEntity {

    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name="return_request_id")
    private ReturnRequestEntity returnRequest;

    @ManyToOne
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
            ProductVariantsEntity variant = stock.getProductVariantsEntity();
            ProductEntity product = variant.getProductEntity();
            AddressRepObj address = (AddressRepObj)subOrder.getAddressEntity().getRepresentation();
            BigDecimal totalPrice = basket.getPrice().subtract(basket.getDiscount()).multiply( new BigDecimal(getReturnedQuantity()));
            dto.setAddress(address);
            dto.setBasketItem(getBasket().getId());
            dto.setShopId(stock.getShopsEntity().getId());
            dto.setPrice(totalPrice);
            dto.setVariantId(variant.getId());
            dto.setFeatureSpec(variant.getFeatureSpec());
            dto.setProductName(product.getName());
            dto.setProductId(product.getId());
        }

        dto.setReceivedQuantity(getReceivedQuantity());
        dto.setReturnedQuantity(getReturnedQuantity());
        dto.setReceivedOn(getReceivedOn());

        return dto;
    }
}
