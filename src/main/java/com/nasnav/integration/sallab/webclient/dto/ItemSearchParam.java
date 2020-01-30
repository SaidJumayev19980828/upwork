package com.nasnav.integration.sallab.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@AllArgsConstructor
@Data
public class ItemSearchParam {

    private String itemNumber;

    @JsonProperty("custTypeNo")
    private Integer customerTypeNo;

    private BigDecimal quantity;

    private BigDecimal discount;

    @JsonProperty("disValue")
    private BigDecimal discountValue;

    @Override
    public String toString() {
        String result = "";

        if (this.itemNumber != null)
            result += "&itemNumber="+this.itemNumber;

        if (this.customerTypeNo != null)
            result += "&custTypeNo="+this.customerTypeNo;

        if (this.quantity != null)
            result += "&quantity="+this.quantity;

        if (this.discount != null)
            result += "&discount="+this.discount;

        if (this.discountValue != null)
            result += "&disValue="+this.discountValue;

        return result.length() > 0 ? result.substring(1) : "";
    }
}
