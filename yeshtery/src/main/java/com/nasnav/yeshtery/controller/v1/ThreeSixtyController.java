package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.dto.ShopThreeSixtyDTO;
import com.nasnav.dto.response.ProductsPositionDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.service.ShopService;
import com.nasnav.service.ShopThreeSixtyService;
import com.nasnav.yeshtery.YeshteryConstants;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping(ThreeSixtyController.API_PATH)
public class ThreeSixtyController {

    static final String API_PATH = YeshteryConstants.API_PATH +"/360view/";

    @Autowired
    private ShopThreeSixtyService shop360Svc;
    @Autowired
    private ShopService shopService;

    @GetMapping(value = "json_data", produces = MediaType.TEXT_PLAIN_VALUE)
    public String getShop360JsonInfo(@RequestParam("shop_id") Long shopId,
                                     @RequestParam String type,
                                     @RequestParam(defaultValue = "true") Boolean published) {
        return shop360Svc.getShop360JsonInfo(shopId, type, published);
    }

    @GetMapping(value = "sections", produces = MediaType.APPLICATION_JSON_VALUE)
    public Map getShop360Sections(@RequestParam("shop_id") Long shopId) {
        Map<String, List> res = new HashMap<>();
        res.put("floors", shop360Svc.getSections(shopId));
        return res;
    }

    @GetMapping(value = "shops", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShopThreeSixtyDTO getShop360Shops(@RequestParam("shop_id") Long shopId) {
        return shop360Svc.getThreeSixtyShops(shopId, true);
    }

    @GetMapping(value = "/shop", produces = MediaType.APPLICATION_JSON_VALUE)
    public ShopRepresentationObject getShopById(@RequestParam("shop_id") Long shopId) {
        return shopService.getShopById(shopId);
    }

    @GetMapping(value = "products_positions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ProductsPositionDTO getShop360ProductsPositions(@RequestParam("shop_id") Long shopId,
                                                           @RequestParam(defaultValue = "2") short published,
                                                           @RequestParam(value = "scene_id", required = false) Long sceneId,
                                                           @RequestParam(value = "section_id", required = false) Long sectionId,
                                                           @RequestParam(value = "floor_id", required = false) Long floorId) {
        return shop360Svc.getProductsPositions(shopId, published, sceneId, sectionId, floorId);
    }

    @GetMapping(value = "products", produces = MediaType.APPLICATION_JSON_VALUE)
    public LinkedHashMap getShop360products(@RequestParam("shop_id") Long shopId,
                                            @RequestParam(required = false) String name,
                                            @RequestParam(required = false, defaultValue = "5") Integer count,
                                            @RequestParam(value = "product_type", required = false) Integer productType,
                                            @RequestParam(value = "has_360", required = false, defaultValue = "false") boolean has360,
                                            @RequestParam(value = "published", required = false) Short published,
                                            @RequestParam(value = "include_out_of_stock", required = false, defaultValue = "false") Boolean includeOutOfStock)
            throws BusinessException {
        return shop360Svc.getShop360Products(shopId, name, count, productType, published, has360, includeOutOfStock);
    }
}
