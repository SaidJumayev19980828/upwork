package com.nasnav.persistence;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import javax.persistence.*;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.navbox.Shipment;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.shipping.model.ShippingEta;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import lombok.Data;

@Entity
@Table(name="shipment")
@Data
public class ShipmentEntity implements BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToOne
	@JoinColumn(name="sub_order_id", referencedColumnName = "id")
	private OrdersEntity subOrder;

	@ManyToOne
	@JoinColumn(name="return_request_id", referencedColumnName = "id")
	private ReturnRequestEntity returnRequest;
	
	private String parameters;
	
	@Column(name="created_at")
	@CreationTimestamp
	private LocalDateTime createdAt;
	
	@Column(name="updated_at")
	@UpdateTimestamp
	private LocalDateTime updatedAt;
	
	@Column(name="status")
	private Integer status;
	
	@Column(name="external_id")
	private String externalId;
	
	@Column(name="track_number")
	private String trackNumber;

	@Column(name="shipping_service_id")
	private String shippingServiceId;

	@Column(name="shipping_fee")
	private BigDecimal shippingFee;

	@Column(name="delivery_from")
	private LocalDate from;
	@Column(name="delivery_until")
	private LocalDate to;

	@Override
	public BaseRepresentationObject getRepresentation() {
		String status = 
				ShippingStatus.getShippingStatusName(this.status);
		
		Shipment shipment = new Shipment();
		shipment.setServiceId(getShippingServiceId());
		shipment.setServiceName(getShippingServiceId());
		shipment.setExternalId(getExternalId());
		shipment.setShippingFee(getShippingFee());
		shipment.setShippingEta(new ShippingEta(getFrom(), getTo()));
		shipment.setTrackingNumber(getTrackNumber());
		shipment.setStatus(status);
		return shipment;
	}
}
