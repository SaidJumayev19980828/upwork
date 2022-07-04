package com.nasnav.commons.model.handler;


import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.dto.VariantUpdateDTO;
import lombok.Data;
import lombok.EqualsAndHashCode;


//TODO Check Duplication DataImportServiceImpl

@Data
@EqualsAndHashCode(callSuper = true)
public class VariantDTOWithExternalIdAndStock extends VariantUpdateDTO {

    private String externalId;

    private StockUpdateDTO stock;

}
