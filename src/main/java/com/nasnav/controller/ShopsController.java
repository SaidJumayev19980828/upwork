package com.nasnav.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    public ResponseEntity updateShop(@RequestHeader(name = "User-Token") String userToken,
                                     @RequestBody ShopJsonDTO shopJson) throws BusinessException {
        ShopResponse response =  shopService.shopModification(shopJson);
        return new ResponseEntity(response, response.getHttpStatus());
    }
    
    
    
    
    
    @ApiOperation(value = "update shop stock of a product variant", nickname = "updateStock")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/stock", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public StockUpdateResponse updateStock(@RequestBody StockUpdateDTO stockUpdateReq) throws BusinessException {      
        return stockService.updateStock(stockUpdateReq);
    }
}
