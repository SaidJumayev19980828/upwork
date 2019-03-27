package com.nasnav.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.nasnav.enumerations.OrderFailedStatus;
import com.nasnav.persistence.OrdersEntity;

import lombok.Data;

import java.io.Serializable;
import java.math.BigDecimal;

import org.springframework.http.HttpStatus;


@Data
public class OrderResponse implements Serializable {

	
    /**
	 * 
	 */
	private static final long serialVersionUID = -2071547898771698563L;

	@JsonProperty
    private boolean success;
	
	@JsonIgnore
	private HttpStatus code;
		
	@JsonIgnore
    private OrdersEntity entity;
	

    // set property name to order_id as per API requirements
    @JsonProperty(value = "order_id")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Long orderId;

    // set property name to price as per API requirements
    @JsonProperty(value = "price")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private BigDecimal price;
    
 // set property name to status as per API requirements
    @JsonProperty(value = "status")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    OrderFailedStatus status;

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

    /**
     * Constructor representing success response
     *
     * @param oderId 
     * @param price
     */
    public OrderResponse(Long orderId, BigDecimal price) {
        this.success = true;
        this.orderId = orderId;
        this.price = price;
        code = HttpStatus.OK;
    }
    
    /**
     * Constructor representing success response
     *
     * @param entity Entity Object 
     */
    public OrderResponse(OrdersEntity entity) {
        this.success = true;
        this.entity = entity;
        code = HttpStatus.OK;
    }


}
