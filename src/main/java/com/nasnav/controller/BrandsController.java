package com.nasnav.controller;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.BrandService;

@RestController
@RequestMapping("/navbox")
public class BrandsController {


    @Autowired
    private BrandService brandService;

    @GetMapping(value = "/brand")
    public ResponseEntity<?> getBrandById(@RequestParam(name = "brand_id") Long brandId) throws BusinessException {

        Organization_BrandRepresentationObject brandRepresentationObject = brandService.getBrandById(brandId);

        if(brandRepresentationObject==null){
            throw new BusinessException("Brand not found",null,HttpStatus.NOT_FOUND);
        }
        JSONObject response = new JSONObject();
        response.put("name",brandRepresentationObject.getName());
        response.put("p_name",brandRepresentationObject.getPName());
        response.put("logo",brandRepresentationObject.getLogoUrl());
        response.put("banner",brandRepresentationObject.getBanner());

        return new ResponseEntity<>(response.toString(), HttpStatus.OK);
    }
}
