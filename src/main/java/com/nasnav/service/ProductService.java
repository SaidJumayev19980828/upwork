package com.nasnav.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.nasnav.dao.ProductRepository;
import com.nasnav.dto.ProductsResponse;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    @Autowired
    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public ProductsResponse getProductsResponseByShopId(Long shopId, Long categoryId, Long start, Long count, String sort, String order) {

        return null;

    }

    public ProductsResponse getProductsResponseByOrganizationId(Long organizationId, Long categoryId, Long start, Long count, String sort, String order) {
        return null;
    }
}
