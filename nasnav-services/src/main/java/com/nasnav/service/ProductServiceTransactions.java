package com.nasnav.service;

import java.util.List;

public interface ProductServiceTransactions {

  void deleteProducts(List<Long> productIds, Boolean forceDelete);

  void deleteVariants(List<Long> variantIds, Boolean forceDelete);

  void deleteBundle(Long bundleId);

}