package com.nasnav.controller;

import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.dto.StockUpdateDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ShopResponse;
import com.nasnav.response.StockUpdateResponse;
import com.nasnav.service.ShopService;
import com.nasnav.service.StockService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/shop")
public class ShopsController {

    @Autowired
    private ShopService shopService;
    
    
    @Autowired
    private StockService stockService;

    @Operation(description =  "update information about shop by its ID or create a New shop", summary = "updateShop")
    @ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 401" ,description = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @ApiResponse(responseCode = " 406" ,description = "INVALID_PARAM")})
    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse updateShop(@RequestHeader(name = "User-Token", required = false) String userToken,
                                     @RequestBody ShopJsonDTO shopJson) {
        return shopService.shopModification(shopJson);
    }
    
    
    
    @Operation(description =  "update shop stock of a product variant", summary = "updateStock")
    @ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 401" ,description = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @ApiResponse(responseCode = " 406" ,description = "INVALID_PARAM")})
    @PostMapping(value = "/stock", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public StockUpdateResponse updateStock(@RequestHeader(name = "User-Token", required = false) String userToken,
                                           @RequestBody StockUpdateDTO stockUpdateReq) throws BusinessException {
        return stockService.updateStock(stockUpdateReq);
    }



    @Operation(description =  "delete shop by its ID", summary = "deleteShop")
    @ApiResponses(value = { @ApiResponse(responseCode = " 200" ,description = "OK"),
            @ApiResponse(responseCode = " 401" ,description = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @ApiResponse(responseCode = " 406" ,description = "INVALID_PARAM")})
    @DeleteMapping(value = "/delete")
    public void deleteShop(@RequestHeader(name = "User-Token", required = false) String userToken,
                           @RequestParam("shop_id") Long id) {
        shopService.deleteShop(id);
    }
}
