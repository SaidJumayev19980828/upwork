package com.nasnav.controller;

import com.nasnav.dto.ShopThreeSixtyDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.ShopThreeSixtyService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/360view")
public class ShopThreeSixtyController {

    @Autowired
    private ShopThreeSixtyService shop360Svc;

    @ApiOperation(value = "Get information about shop 360 json data", nickname = "getShop360Data")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/json_data", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getShop360JsonInfo(@RequestParam("shop_id") Long shopId,
                                                   @RequestParam("type") String type) {
        return new ResponseEntity<>(shop360Svc.getShop360JsonInfo(shopId, type), HttpStatus.OK);
    }



    @ApiOperation(value = "Get information about shop 360 product positions", nickname = "getShop360ProductPositions")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/product_positions", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getShop360ProductPositions(@RequestParam("shop_id") Long shopId) {
        return new ResponseEntity<>(shop360Svc.getProductPositions(shopId), HttpStatus.OK);
    }



    @ApiOperation(value = "Get list of shop360s of specific shop", nickname = "getShop360Shops")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/shops", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List getShop360Shops(@RequestParam("shop_id") Long shopId) {
        return shop360Svc.getThreeSixtyShops(shopId);
    }


    @ApiOperation(value = "Create/Update shop360", nickname = "updateShop360")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/shops", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse updateThreeSixtyShop(@RequestHeader("User-Token") String userToken,
                                        @RequestParam ShopThreeSixtyDTO shopThreeSixtyDTO) throws BusinessException {
        return shop360Svc.updateThreeSixtyShop(shopThreeSixtyDTO);
    }
}
