package com.nasnav.service.model.importproduct.csv;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class OrderRow {
        private Long orderId;
        private String userName;
        private String shopName;
        private BigDecimal shipping;
        private String shippingService;
        private String paymentOperator;
        private BigDecimal subtotal;
        private BigDecimal total;
        private String currency;
        private String notes;
        private String status;
        private Integer totalQuantity;
        private String paymentStatus;
        private String shippingStatus;
        private BigDecimal discount;
        private LocalDateTime creationDate;
        private String flatNumber;
        private String buildingNumber;
        private String phoneNumber;
        private String area;
        private String city;
        private String country;
        private String subArea;
        private String addressLine1;
        private String addressLine2;
}
