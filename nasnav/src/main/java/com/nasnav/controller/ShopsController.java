package com.nasnav.controller;

import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.dto.request.ShopIdAndPriority;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ShopResponse;
import com.nasnav.response.StockUpdateResponse;
import com.nasnav.service.ShopService;
import com.nasnav.service.StockService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping("/shop")
public class ShopsController {

    @Autowired
    private ShopService shopService;
    @Autowired
    private StockService stockService;
    
    @PostMapping(value = "/update", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public ShopResponse updateShop(@RequestHeader(name = "User-Token", required = false) String userToken,
                                     @RequestBody ShopJsonDTO shopJson) {
        return shopService.shopModification(shopJson);
    }

    @PostMapping(value = "/stock", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    public StockUpdateResponse updateStock(@RequestHeader(name = "User-Token", required = false) String userToken,
                                           @RequestBody StockUpdateDTO stockUpdateReq) throws BusinessException {
        return stockService.updateStock(stockUpdateReq);
    }

    @DeleteMapping(value = "/stock")
    public void deleteStocks(@RequestHeader(name = "User-Token", required = false) String userToken,
                             @RequestParam(value = "shop_id", required = false) Long shopId) {
        stockService.deleteStocks(shopId);
    }

    @DeleteMapping(value = "/delete")
    public void deleteShop(@RequestHeader(name = "User-Token", required = false) String userToken,
                           @RequestParam("shop_id") Long id) {
        shopService.deleteShop(id);
    }

    @PostMapping(value = "/priority", consumes = APPLICATION_JSON_VALUE)
    public void changeShopsPriority(@RequestHeader(name = "User-Token", required = false) String userToken,
                                    @RequestBody List<ShopIdAndPriority> dto) {
        shopService.changeShopsPriority(dto);
    }
}
