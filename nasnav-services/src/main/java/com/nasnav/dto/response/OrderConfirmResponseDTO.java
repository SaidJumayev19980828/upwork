package com.nasnav.dto.response;

import com.nasnav.shipping.model.ShipmentTracker;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderConfirmResponseDTO {
	private String shippingBill;
	private String fileName;
	private String mimeType;

	public OrderConfirmResponseDTO(ShipmentTracker tracker){
		this.shippingBill = tracker.getAirwayBillFile();
		this.fileName = tracker.getAirwayBillFileName();
		this.mimeType = tracker.getAirwayBillFileMime();
	}
}
