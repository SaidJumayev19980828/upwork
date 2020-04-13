package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.*;
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

import java.io.IOException;
import java.io.UnsupportedEncodingException;
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
    @GetMapping(value = "/json_data", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getShop360JsonInfo(@RequestParam("shop_id") Long shopId,
                                                   @RequestParam("type") String type) {
        return new ResponseEntity<>(shop360Svc.getShop360JsonInfo(shopId, type), HttpStatus.OK);
    }



    @ApiOperation(value = "Get information about shop 360 product positions", nickname = "getShop360ProductPositions")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/product_positions", produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity getShop360ProductPositions(@RequestParam("shop_id") Long shopId) {
        return new ResponseEntity<>(shop360Svc.getProductPositions(shopId), HttpStatus.OK);
    }


    @ApiOperation(value = "Get information about shop 360 sections", nickname = "getShop360Sections")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/sections", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity getShop360Sections(@RequestParam("shop_id") Long shopId) {
        Map<String, List> res = new HashMap<>();
        res.put("floors", shop360Svc.getSections(shopId));
        return new ResponseEntity<>(res, HttpStatus.OK);
    }


    @ApiOperation(value = "Get shop360s of specific shop", nickname = "getShop360s")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/shops", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopThreeSixtyDTO getShop360Shops(@RequestParam("shop_id") Long shopId) {
        return shop360Svc.getThreeSixtyShops(shopId);
    }


    @ApiOperation(value = "Create/Update shop360", nickname = "updateShop360")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/shops", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse updateThreeSixtyShop(@RequestHeader("User-Token") String userToken,
                                             @RequestBody ShopThreeSixtyDTO shopThreeSixtyDTO)
            throws BusinessException {
        return shop360Svc.updateThreeSixtyShop(shopThreeSixtyDTO);
    }


    @ApiOperation(value = "Create/Update shop360 json data", nickname = "updateShop360JsonData")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/json_data", consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse updateThreeSixtyShopJsonData(@RequestHeader("User-Token") String userToken,
                                                     @RequestParam("shop_id") Long shopId,
                                                     @RequestParam String type,
                                                     @RequestBody String json_data)
            throws BusinessException, UnsupportedEncodingException {
        return shop360Svc.updateThreeSixtyShopJsonData(shopId, type, json_data);
    }

    @ApiOperation(value = "Create/Update shop360 product positions", nickname = "updateShop360ProductPositions")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/product_positions",consumes = MediaType.APPLICATION_JSON_VALUE,
                 produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse updateThreeSixtyShopProductPositions(@RequestHeader("User-Token") String userToken,
                                                             @RequestParam("shop_id") Long shopId,
                                                             @RequestBody String json_data)
            throws BusinessException, UnsupportedEncodingException {
        return shop360Svc.updateThreeSixtyShopProductPositions(shopId, json_data);
    }

    @ApiOperation(value = "Create/Update shop360 sections", nickname = "updateShop360Sections")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/sections", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse updateThreeSixtyShopSections(@RequestHeader("User-Token") String userToken,
                                                     @RequestParam("shop_id") Long shopId,
                                                     @RequestBody List<ShopFloorsRequestDTO> jsonDTO)
            throws BusinessException, IOException {
        return shop360Svc.updateThreeSixtyShopSections(shopId, jsonDTO);
    }


    @ApiOperation(value = "Search for products related to 360", nickname = "get360sProducts")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/products", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public List<ProductRepresentationObject> getShop360products(@RequestParam("org_id") Long orgId,
                                                                @RequestParam("barcode") String barcode,
                                                                @RequestParam("name") String name) throws BusinessException {
        return shop360Svc.getShop360Products(orgId, barcode, name);
    }
}
