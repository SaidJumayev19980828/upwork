package com.nasnav.controller;

import com.nasnav.exceptions.BusinessException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/navbox")
public class CategoryController {

    @GetMapping("/categories/{organization_id}")
    public ResponseEntity<?> getOrganizationCategories(@PathVariable("organization_id") Long organizationId) throws BusinessException {

        return null;
    }

}
