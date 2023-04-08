package com.nasnav.service;

import java.util.List;
import java.util.Set;

import com.nasnav.dto.AddonDetailsDTO;
import com.nasnav.dto.AddonStockDTO;
import com.nasnav.dto.AddonStocksDTO;
import com.nasnav.dto.AddonsDTO;
import com.nasnav.dto.ProductAddonDTO;
import com.nasnav.dto.ProductAddonsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.AddonEntity;
import com.nasnav.persistence.AddonStocksEntity;
import com.nasnav.persistence.BasketsEntity;
import com.nasnav.response.AddonResponse;
import com.nasnav.service.model.ProductAddonPair;

public interface AddonService {

  AddonEntity createOrUpdateAddon(AddonsDTO addonDTO) throws BusinessException;

  List<AddonEntity> findAllAddonPerOrganization();

  AddonResponse deleteOrgAddon(Long addonId) throws BusinessException;

  boolean updateProductAddons(ProductAddonDTO productAddonDTO) throws BusinessException;

  void addAddonsToProducts(Set<ProductAddonPair> newProductAddons);

  boolean deleteProductAddons(List<Long> productIds, List<Long> addonsIds) throws BusinessException;

  AddonStocksEntity createOrUpdateAddonStock(AddonStockDTO addonStockDTO) throws BusinessException;

  void deleteAddonStock(Long id, Long shopId, Long addonId) throws BusinessException;

  List<AddonStocksDTO> getAllAddonStocks(Long shopId);

  void deleteAddonFromProduct(Long addonItemId) throws BusinessException;

  List<AddonDetailsDTO> listItemAddons(Long itemId);

  List<ProductAddonsDTO> getProductAddonsInStock(Long productId, Long shopeId) throws BusinessException;

List<AddonDetailsDTO> listItemAddonsPreSave(BasketsEntity entity);

}