package com.nasnav.yeshtery.controller.v1;

import com.nasnav.dto.ProductRepresentationObject;
import com.nasnav.dto.ShopRepresentationObject;
import com.nasnav.service.ProductService;
import com.nasnav.service.ShopService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/v1/yeshtery")
@Tag(name = "Yeshtery Controller")
@CrossOrigin("*")
public class YeshteryController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private ProductService productService;

    @GetMapping(value = "/location_shops", produces = APPLICATION_JSON_VALUE)
    public List<ShopRepresentationObject> shippingCallback(@PathVariable("api_version") String apiVersion,
                                                           @RequestParam(value = "name", required = false, defaultValue = "") String name) {
        return shopService.getLocationShops(name);
    }



    @GetMapping(value = "/related_products", produces = APPLICATION_JSON_VALUE)
    public List<ProductRepresentationObject> getRelatedProducts(@PathVariable("api_version") String apiVersion,
                                                                @RequestParam("product_id") Long productId) {
        return productService.getRelatedProducts(productId);
    }
}
