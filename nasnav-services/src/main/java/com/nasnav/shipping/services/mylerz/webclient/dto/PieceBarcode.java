package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class PieceBarcode {
    @JsonProperty("Barcode")
    private String barcode;
}
