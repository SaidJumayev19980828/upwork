package com.nasnav.controller;

import com.nasnav.dto.OrganizationRepresentationObject;
import com.nasnav.service.OrganizationService;
import com.nasnav.service.ShopService;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.nasnav.dto.Organization_BrandRepresentationObject;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.BrandService;

@RestController
@RequestMapping("/navbox")
public class NavboxController {

    @Autowired
    private BrandService brandService;

    @Autowired
    private ShopService shopService;

    @Autowired
    private OrganizationService organizationService;


    @GetMapping("/categories/{organization_id}")
    public ResponseEntity<?> getOrganizationCategories(@PathVariable("organization_id") Long organizationId) throws BusinessException {

        return null;
    }

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

    @GetMapping(value="/organization")
    @ResponseStatus(value = HttpStatus.OK)
    public @ResponseBody
    OrganizationRepresentationObject getOrganizationByName(@RequestParam(name="p_name",required=false) String organizationName, @RequestParam(name="org_id",required=false) Long organizationId) throws BusinessException {

        if(organizationName==null && organizationId==null)
            throw new BusinessException("Provide org_id or p_name request params", null, HttpStatus.BAD_REQUEST);

        if(organizationName!=null)
            return organizationService.getOrganizationByName(organizationName);

        return organizationService.getOrganizationById(organizationId);
    }


    @GetMapping("/shops")
    public ResponseEntity<?> getShopByIdOrShopsByOrganization(
            @RequestParam(name = "shop_id", required = false) Long shopId,
            @RequestParam(name = "org_id", required = false) Long orgId) throws BusinessException {

        if (shopId == null && orgId == null) {
            throw new BusinessException("Provide either shop_id or org_id request param", null, HttpStatus.BAD_REQUEST);
        }
        if (shopId != null)
            return new ResponseEntity<>(shopService.getShopById(shopId), HttpStatus.OK);

        return new ResponseEntity<>(shopService.getOrganizationShops(orgId), HttpStatus.OK);
    }
}
