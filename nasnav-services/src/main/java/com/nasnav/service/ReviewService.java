package com.nasnav.service;

import com.nasnav.dto.request.product.ProductRateDTO;
import com.nasnav.dto.response.navbox.ProductRateRepresentationObject;

import java.util.List;
import java.util.Set;

public interface ReviewService {

    void rateProduct(ProductRateDTO dto);
    void approveRate(Long rateId);
    List<ProductRateRepresentationObject> getProductRatings(Long variantId);
    List<ProductRateRepresentationObject> getProductsRatings();
    List<ProductRateRepresentationObject> getYeshteryVariantRatings(Long variantId);
    List<ProductRateRepresentationObject> getUserProductsRatings(Set<Long> variantIds);
}
