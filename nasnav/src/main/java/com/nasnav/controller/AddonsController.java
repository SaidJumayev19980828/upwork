package com.nasnav.controller;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

import java.util.List;

import com.nasnav.dto.AddonDetailsDTO;
import com.nasnav.dto.AddonStockDTO;
import com.nasnav.dto.AddonStocksDTO;
import com.nasnav.dto.AddonsDTO;
import com.nasnav.dto.ProductAddonDTO;
import com.nasnav.dto.ProductAddonsDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.AddonEntity;
import com.nasnav.persistence.AddonStocksEntity;
import com.nasnav.response.AddonResponse;
import com.nasnav.response.AddonStockResponse;
import com.nasnav.service.AddonService;

@RestController
@RequestMapping("/addons")
@CrossOrigin("*")
public class AddonsController {

	@Autowired
	AddonService addonService;

	@PostMapping(value = "", produces = APPLICATION_JSON_VALUE)
	public AddonResponse addOrUpdateOrganizationAddon(
			@RequestHeader(name = "User-Token", required = false) String userToken, @RequestBody AddonsDTO addonDTO)
			throws BusinessException {
		AddonEntity addon = addonService.createOrUpdateAddon(addonDTO);
		return new AddonResponse(addon.getId());
	}

	@GetMapping(value = "", produces = APPLICATION_JSON_VALUE)
	public List<AddonEntity> getOrganiztionAddons(
			@RequestHeader(name = "User-Token", required = false) String userToken) throws BusinessException {

		return addonService.findAllAddonPerOrganization();
	}

	@DeleteMapping(value = "", produces = APPLICATION_JSON_VALUE)
	public AddonResponse deleteOrganizationAddon(@RequestHeader(name = "User-Token", required = false) String userToken,
			@RequestParam(value = "addon_id") Long addonId) throws BusinessException {
		return addonService.deleteOrgAddon(addonId);
	}

	@PostMapping(value = "product", consumes = APPLICATION_JSON_VALUE)
	public void updateProductAddons(@RequestHeader(name = "User-Token", required = false) String token,
			@RequestBody ProductAddonDTO productAddonDTO) throws BusinessException {
		addonService.updateProductAddons(productAddonDTO);
	}

	@DeleteMapping(value = "product")
	public void deleteProductAddons(@RequestHeader(name = "User-Token", required = false) String token,
			@RequestParam("products_ids") List<Long> productIds, @RequestParam("addons_ids") List<Long> addonsIds)
			throws BusinessException {
		addonService.deleteProductAddons(productIds, addonsIds);
	}

	@PostMapping(value = "stock", consumes = APPLICATION_JSON_VALUE)
	public AddonStockResponse addUpdateAddonsStock(@RequestHeader(name = "User-Token", required = false) String token,
			@RequestBody AddonStockDTO addonStockDTO) throws BusinessException {
		AddonStocksEntity res = addonService.createOrUpdateAddonStock(addonStockDTO);
		return new AddonStockResponse(res.getId());
	}

	@DeleteMapping(value = "stock")
	public void deleteAddonsStock(@RequestHeader(name = "User-Token", required = false) String token,
			@RequestParam(name = "id", required = true) Long id,
			@RequestParam(name = "shop_id", required = true) Long shopId,
			@RequestParam(name = "addon_id", required = true) Long addonId) throws BusinessException {
		addonService.deleteAddonStock(id, shopId, addonId);

	}

	@GetMapping(value = "stock", produces = APPLICATION_JSON_VALUE)
	public List<AddonStocksDTO> getAllAddonsStock(@RequestHeader(name = "User-Token", required = false) String token,
												  @RequestParam(name = "shop_id", required = true) Long shopId) throws BusinessException {
		return addonService.getAllAddonStocks(shopId);

	}
	
	@DeleteMapping(value = "item")
	public void deleteAddonFromProduct(@RequestHeader(name = "User-Token", required = false) String token,
			@RequestParam(name = "addon_item_id", required = true) Long addonItemId) throws BusinessException {
		addonService.deleteAddonFromProduct(addonItemId);

	}
	
	@GetMapping(value = "item", produces = APPLICATION_JSON_VALUE)
	public List<AddonDetailsDTO> getItemAddons(@RequestHeader(name = "User-Token", required = false) String token,
											   @RequestParam(name = "item_id", required = true) Long itemId) throws BusinessException {
		return addonService.listItemAddons(itemId);

	}
	@GetMapping(value = "product", produces = APPLICATION_JSON_VALUE)
	public List<ProductAddonsDTO> getProductAddonsInStock(@RequestHeader(name = "User-Token", required = false) String token,
														  @RequestParam(name = "product_id" )Long productId, @RequestParam(name = "shop_id" )Long shopId) throws BusinessException{
      return addonService.getProductAddonsInStock(productId,shopId);
}
}