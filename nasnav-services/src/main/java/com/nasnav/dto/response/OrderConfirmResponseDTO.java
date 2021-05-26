package com.nasnav.dto.response;

import com.nasnav.shipping.model.ShipmentTracker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfrimResponseDTO {
	private String shippingBill;
}
