package com.nasnav.service;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.LinkedHashMap;
import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Transactional;

import com.nasnav.dto.ShopFloorsRequestDTO;
import com.nasnav.dto.ShopThreeSixtyDTO;
import com.nasnav.dto.request.ProductPositionDTO;
import com.nasnav.dto.response.FloorsData;
import com.nasnav.dto.response.PostProductPositionsResponse;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ShopResponse;

public interface ShopThreeSixtyService {

  String getShop360JsonInfo(Long shopId, String type, Boolean publish);

  ProductsPositionDTO getProductsPositions(Long shopId, short published, Long sceneId, Long sectionId, Long floorId);

  FloorsData getFloorsData(Long shopId);

  ShopThreeSixtyDTO getThreeSixtyShops(Long shopId, boolean yeshteryState);

  ShopResponse updateThreeSixtyShop(ShopThreeSixtyDTO shopThreeSixtyDTO);

  ShopResponse updateThreeSixtyShopJsonData(Long shopId, String type, String dataDTO)
      throws UnsupportedEncodingException;

  PostProductPositionsResponse updateThreeSixtyShopProductsPositions(Long shopId, List<ProductPositionDTO> json);

  ShopResponse updateThreeSixtyShopSections(Long shopId, List<ShopFloorsRequestDTO> jsonDTO)
      throws BusinessException, IOException;

  LinkedHashMap<String, List<ThreeSixtyProductsDTO>> getShop360Products(Long shopId, String name, Integer count,
      Integer productType,
      Short published, boolean has360, boolean includeOutOfStock) throws BusinessException;

  ShopResponse publishJsonData(Long shopId);

  void deleteShop360Floors(Long shopId);

  void deleteShop360Floor(Long shopId, Long floorId, boolean confirm);

  void deleteShop360Section(Long sectionId, boolean confirm);

  void deleteShop360Scene(Long sceneId, boolean confirm);

  void exportThreeSixtyImages(Long shopId, HttpServletResponse response) throws IOException;

}