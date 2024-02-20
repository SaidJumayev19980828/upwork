package com.nasnav.persistence.dto.query.result;

import lombok.Data;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;

@Data
public class CartCheckoutDTO {
   private HashMap<Long, List<CartCheckoutData>> CartCheckoutData;
   private BigDecimal discount;
   private BigDecimal total;
   private BigDecimal subTotal;

}
