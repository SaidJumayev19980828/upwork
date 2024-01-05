package com.nasnav.service.model.importproduct.csv;

import com.nasnav.dto.DetailedOrderRepObject;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderRow {
    private Long orderId ;
    private String shop;
    private String shipping;
    private String trackingNumber;
    private Integer totalQuantity;
    private String userName;
    private BigDecimal total;
    private String paymentStatus;
    private String status;
    private String shippingStatus;
    private BigDecimal discount;
    private LocalDateTime createdAt;
    private LocalDateTime deliveryDate;
    private String addressLine2;
    private BigDecimal latitude;
    private BigDecimal longitude;
    private String postalCode;
    private String phoneNumber;
    private String area;
    private String city;
    private String country;

    public OrderRow(DetailedOrderRepObject order) {
        this.orderId = order.getOrderId();
        this.shop = order.getShopName();
        this.shipping = order.getShippingService();
        this.trackingNumber = order.getTrackNumber();
        this.totalQuantity = order.getTotalQuantity();
        this.userName = order.getUserName();
        this.total = order.getTotal();
        this.paymentStatus = order.getPaymentStatus();
        this.status = order.getStatus();
        this.shippingStatus = order.getShippingStatus();
        this.discount = order.getDiscount();
        this.createdAt = order.getCreatedAt();
        this.deliveryDate = order.getDeliveryDate();

        if (order.getShippingAddress() != null) {
            this.addressLine2 = order.getShippingAddress().getAddressLine2();
            this.latitude = order.getShippingAddress().getLatitude();
            this.longitude = order.getShippingAddress().getLongitude();
            this.postalCode = order.getShippingAddress().getPostalCode();
            this.phoneNumber = order.getShippingAddress().getPhoneNumber();
            this.area = order.getShippingAddress().getArea();
            this.city = order.getShippingAddress().getCity();
            this.country = order.getShippingAddress().getCountry();
        }
    }
}
