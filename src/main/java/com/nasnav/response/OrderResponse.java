package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.dto.DetailedOrderRepObject;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.dto.OrderRepresentationObject;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.List;

import org.springframework.http.HttpStatus;


@Data
@EqualsAndHashCode(callSuper = false)
public class OrderResponse  implements Serializable {

	private static final long serialVersionUID = -2071547898771698563L;

	@JsonIgnore
    private boolean success;

	@JsonIgnore
	private HttpStatus code;

    // set property name to order_id as per API requirements
    @JsonProperty(value = "order_id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long orderId;

    // set property name to price as per API requirements
    @JsonProperty(value = "order_total")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BigDecimal price;
    
 // set property name to responseStatuses as per API requirements
    @JsonProperty(value = "status")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    OrderFailedStatus status;

    @JsonProperty(value = "orders")
    private List<OrderRepresentationObject> orders;

    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    @JsonIgnore
    private DetailedOrderRepObject detailedOrder;

    public OrderResponse() {
    }
    
   
    /**
     * Constructor representing failed response
     *
     * @param status OrderFailedStatus
     */
    public OrderResponse(OrderFailedStatus status, HttpStatus code) {
    	this();
    	this.status = status;
    	this.code = code;
    }

    public OrderResponse(Long orderId, BigDecimal price) {
        this.orderId = orderId;
        this.price = price;
        code = HttpStatus.OK;
    }

    public OrderResponse(List<OrderRepresentationObject> orders, BigDecimal total) {
        this.orders = orders;
        this.price = total;
        code = HttpStatus.OK;
    }

}
