package com.nasnav.persistence.dto.query.result;

import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class optimizeCartDTO {
   private  List<CartCheckoutData> cartCheckoutData;
   private  BigDecimal discount;
   private  BigDecimal total;
   private  BigDecimal subTotal;
}
