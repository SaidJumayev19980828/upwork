package com.nasnav.persistence;

import com.nasnav.dto.BaseRepresentationObject;
import com.nasnav.dto.response.navbox.Shipment;
import com.nasnav.enumerations.ShippingStatus;
import com.nasnav.shipping.model.ShippingEta;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.CascadeType.MERGE;
import static javax.persistence.CascadeType.PERSIST;

@Entity
@Table(name="return_shipment")
@Data
public class ReturnShipmentEntity implements BaseEntity {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	@OneToMany(mappedBy = "returnShipment", cascade = {PERSIST, MERGE})
	@EqualsAndHashCode.Exclude
	@ToString.Exclude
	private List<ReturnRequestItemEntity> returnRequestItems;

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


	public ReturnShipmentEntity(){
		returnRequestItems = new ArrayList<>();
	}


	@Override
	public BaseRepresentationObject getRepresentation() {
		String status = 
				ShippingStatus.getShippingStatusName(this.status);
		
		Shipment shipment = new Shipment();
		shipment.setServiceId(getShippingServiceId());
		shipment.setServiceName(getShippingServiceId());
		shipment.setExternalId(getExternalId());
		shipment.setTrackingNumber(getTrackNumber());
		shipment.setStatus(status);
		return shipment;
	}


	public void addReturnItem(ReturnRequestItemEntity item){
		item.setReturnShipment(this);
		returnRequestItems.add(item);
	}
}
