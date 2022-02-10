package com.nasnav.shipping.services.mylerz.webclient.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class Piece {
    private Long pieceNo;
    @JsonProperty("Weight")
    private Double weight;
    @JsonProperty("ItemCategory")
    private String itemCategory;
    @JsonProperty("SpecialNotes")
    private String specialNotes;
    @JsonProperty("Dimensions")
    private String dimensions;
}
