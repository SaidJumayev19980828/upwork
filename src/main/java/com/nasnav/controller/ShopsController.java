package com.nasnav.controller;

import com.nasnav.exceptions.RuntimeBusinessException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ShopResponse;
import com.nasnav.response.StockUpdateResponse;
import com.nasnav.service.ShopService;
import com.nasnav.service.StockService;

import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;

@RestController
@RequestMapping("/shop")
public class ShopsController {

    @Autowired
    private ShopService shopService;
    
    
    @Autowired
    private StockService stockService;

    @ApiOperation(value = "update information about shop by its ID or create a New shop", nickname = "updateShop")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse updateShop(@RequestHeader(name = "User-Token", required = false) String userToken,
                                     @RequestBody ShopJsonDTO shopJson) {
        return shopService.shopModification(shopJson);
    }
    
    
    
    @ApiOperation(value = "update shop stock of a product variant", nickname = "updateStock")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/stock", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public StockUpdateResponse updateStock(@RequestHeader(name = "User-Token", required = false) String userToken,
                                           @RequestBody StockUpdateDTO stockUpdateReq) throws BusinessException {
        return stockService.updateStock(stockUpdateReq);
    }



    @ApiOperation(value = "delete shop by its ID", nickname = "deleteShop")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @DeleteMapping(value = "/delete")
    public void deleteShop(@RequestHeader(name = "User-Token", required = false) String userToken,
                           @RequestParam("shop_id") Long id) {
        shopService.deleteShop(id);
    }
}
