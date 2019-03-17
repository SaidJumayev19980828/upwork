package com.nasnav.controller;

import com.nasnav.dto.ProductSortOptions;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.ProductService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/navbox")
public class ProductController {

    private ProductService productService;

    @GetMapping("/products")
    public ResponseEntity<?> getProducts(@RequestParam(name = "org_id",required = false) Long organizationId,
                                         @RequestParam(name = "shop_id",required = false) Long shopId,
                                         @RequestParam(name = "category_id",required = false) Long categoryId,
                                         @RequestParam(name = "start",required = false) Long start,
                                         @RequestParam(name = "count",required = false) Long count,
                                         @RequestParam(name = "sort",required = false) String sort,
                                         @RequestParam(name = "order",required = false) String order) throws BusinessException {

        if(organizationId==null && shopId==null)
            throw new BusinessException("Shop Id or Organization Id shall be provided",null, HttpStatus.BAD_REQUEST);


        if (sort!=null && ProductSortOptions.getProductSortOptions(sort)==null)
            throw new BusinessException("Sort is limited to id, name, pname, price",null, HttpStatus.BAD_REQUEST);

        if(order!=null && !order.equals("asc") && !order.equals("desc"))
            throw new BusinessException("Order is limited to asc and desc only",null, HttpStatus.BAD_REQUEST);


        return null;
    }

}
