package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.ShopService;

@RestController
public class ShopController {

	@Autowired
	private ShopService shopService;

	@GetMapping("/navbox/shops")
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
