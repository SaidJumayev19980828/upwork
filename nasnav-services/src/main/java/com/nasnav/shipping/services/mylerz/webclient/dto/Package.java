package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Package {
    private String packageNo;
    @JsonProperty("Reference")
    private String reference;
    @JsonProperty("Reference2")
    private String reference2;
    @JsonProperty("BarCode")
    private String barCode;
    @JsonProperty("Status")
    private String status;
    @JsonProperty("Pieces")
    private List<PieceBarcode> pieces;
    @JsonProperty("ErrorCode")
    private String errorCode;
    @JsonProperty("ErrorMessage")
    private String errorMessage;
}
