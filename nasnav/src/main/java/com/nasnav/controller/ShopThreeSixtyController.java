package com.nasnav.controller;

import com.nasnav.dto.ShopFloorDTO;
import com.nasnav.dto.ShopFloorsRequestDTO;
import com.nasnav.dto.ShopThreeSixtyDTO;
import com.nasnav.dto.request.ProductPositionDTO;
import com.nasnav.dto.response.PostProductPositionsResponse;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.dto.response.navbox.ThreeSixtyProductsDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.ShopThreeSixtyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/360view")
public class ShopThreeSixtyController {

    @Autowired
    private ShopThreeSixtyService shop360Svc;
    
    @GetMapping(value = "/json_data", produces = APPLICATION_JSON_VALUE)
    public String getShop360JsonInfo(@RequestParam("shop_id") Long shopId,
                                             @RequestParam String type,
                                             @RequestParam(defaultValue = "true") Boolean published) {
        return shop360Svc.getShop360JsonInfo(shopId, type, published);
    }

    @GetMapping(value = "/sections", produces = APPLICATION_JSON_VALUE)
    public Map<String, List<ShopFloorDTO>> getShop360Sections(@RequestParam("shop_id") Long shopId) {
        Map<String, List<ShopFloorDTO>> res = new HashMap<>();
        res.put("floors", shop360Svc.getSections(shopId));
        return res;
    }

    @GetMapping(value = "/shops", produces = APPLICATION_JSON_VALUE)
    public ShopThreeSixtyDTO getShop360Shops(@RequestParam("shop_id") Long shopId) {
        return shop360Svc.getThreeSixtyShops(shopId, false);
    }

    @PostMapping(value = "/publish", produces = APPLICATION_JSON_VALUE)
    public ShopResponse publishJsonData(@RequestHeader(name = "User-Token", required = false) String userToken,
                                        @RequestParam ("shop_id")Long shopId) {
        return shop360Svc.publishJsonData(shopId);
    }

    @PostMapping(value = "/shops", produces = APPLICATION_JSON_VALUE)
    public ShopResponse updateThreeSixtyShop(@RequestHeader(name = "User-Token", required = false) String userToken,
                                             @RequestBody ShopThreeSixtyDTO shopThreeSixtyDTO) {
        return shop360Svc.updateThreeSixtyShop(shopThreeSixtyDTO);
    }

    @PostMapping(value = "/json_data", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ShopResponse updateThreeSixtyShopJsonData(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                     @RequestParam("shop_id") Long shopId,
                                                     @RequestParam String type,
                                                     @RequestBody String json_data) throws UnsupportedEncodingException {
        return shop360Svc.updateThreeSixtyShopJsonData(shopId, type, json_data);
    }

    @PostMapping(value = "/products_positions",consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public PostProductPositionsResponse updateThreeSixtyShopProductsPositions(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                                              @RequestParam("shop_id") Long shopId,
                                                                              @RequestBody List<ProductPositionDTO>  data) throws BusinessException {
        return shop360Svc.updateThreeSixtyShopProductsPositions(shopId, data);
    }

    @GetMapping(value = "/products_positions", produces = APPLICATION_JSON_VALUE)
    public ProductsPositionDTO getShop360ProductsPositions(@RequestParam("shop_id") Long shopId,
                                                           @RequestParam(defaultValue = "2") short published,
                                                           @RequestParam(value = "scene_id", required = false) Long sceneId,
                                                           @RequestParam(value = "section_id", required = false) Long sectionId,
                                                           @RequestParam(value = "floor_id", required = false) Long floorId) {
        return shop360Svc.getProductsPositions(shopId, published, sceneId, sectionId, floorId);
    }

    @PostMapping(value = "/sections", produces = APPLICATION_JSON_VALUE)
    public ShopResponse updateThreeSixtyShopSections(@RequestHeader(name = "User-Token", required = false) String userToken,
                                                     @RequestParam("shop_id") Long shopId,
                                                     @RequestBody List<ShopFloorsRequestDTO> jsonDTO) throws BusinessException, IOException {
        return shop360Svc.updateThreeSixtyShopSections(shopId, jsonDTO);
    }

    @GetMapping(value = "/products", produces = APPLICATION_JSON_VALUE)
    public LinkedHashMap<String, List<ThreeSixtyProductsDTO>> getShop360products(
             @RequestParam("shop_id") Long shopId,
             @RequestParam(required = false) String name,
             @RequestParam(required = false, defaultValue = "5") Integer count,
             @RequestParam(value = "product_type", required = false) Integer productType,
             @RequestParam(value = "has_360", required = false, defaultValue = "false") boolean has360,
             @RequestParam(value = "published", required = false) Short published,
             @RequestParam(value = "include_out_of_stock", required = false, defaultValue = "false") Boolean includeOutOfStock) throws BusinessException {
        return shop360Svc.getShop360Products(shopId, name, count, productType, published, has360, includeOutOfStock);
    }

    @DeleteMapping(value = "/floors")
    public void deleteShop360Floors(@RequestHeader(name = "User-Token", required = false) String userToken,
                                    @RequestParam("shop_id") Long shopId) {
        shop360Svc.deleteShop360Floors(shopId);
    }

    @DeleteMapping(value = "/floor")
    public void deleteShop360Floor(@RequestHeader(name = "User-Token", required = false) String userToken,
                                   @RequestParam("shop_id") Long shopId,
                                   @RequestParam("floor_id") Long floorId,
                                   @RequestParam(value = "products_positions_confirm", defaultValue = "false") boolean confirm) {
        shop360Svc.deleteShop360Floor(shopId, floorId, confirm);
    }

    @DeleteMapping(value = "/section")
    public void deleteShop360Section(@RequestHeader(name = "User-Token", required = false) String userToken,
                                     @RequestParam("section_id") Long sectionId,
                                     @RequestParam(value = "products_positions_confirm", defaultValue = "false") boolean confirm) throws BusinessException {
        shop360Svc.deleteShop360Section(sectionId, confirm);
    }

    @DeleteMapping(value = "/scene")
    public void deleteShop360Scene(@RequestHeader(name = "User-Token", required = false) String userToken,
                                   @RequestParam("scene_id") Long sceneId,
                                   @RequestParam(value = "products_positions_confirm", defaultValue = "false") boolean confirm) {
        shop360Svc.deleteShop360Scene(sceneId, confirm);
    }
}
