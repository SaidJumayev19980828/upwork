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
    @GetMapping(value = "/json_data", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getShop360JsonInfo(@RequestParam("shop_id") Long shopId,
                                             @RequestParam String type,
                                             @RequestParam(defaultValue = "true") Boolean published) {
        return new ResponseEntity<>(shop360Svc.getShop360JsonInfo(shopId, type, published), HttpStatus.OK);
    }



    @ApiOperation(value = "Get information about shop 360 product positions", nickname = "getShop360ProductPositions")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @GetMapping(value = "/product_positions", produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity getShop360ProductPositions(@RequestParam("shop_id") Long shopId,
                                                     @RequestParam(defaultValue = "true") Boolean published) {
        return new ResponseEntity<>(shop360Svc.getProductPositions(shopId, published), HttpStatus.OK);
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


    @ApiOperation(value = "publish json data", nickname = "publishJsonData")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/publish", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ShopResponse publishJsonData(@RequestHeader("User-Token") String userToken,
                                        @RequestParam ("shop_id")Long shopId) throws BusinessException {
        return shop360Svc.publishJsonData(shopId);
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
                                                                @RequestParam("name") String name) throws BusinessException {
        return shop360Svc.getShop360Products(orgId, name);
    }


    @ApiOperation(value = "Delete shop 360 floors", nickname = "delete360sFloors")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "Deleted"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @DeleteMapping(value = "/floors")
    public void deleteShop360Floors(@RequestHeader("User-Token") String userToken,
                                    @RequestParam("shop_id") Long shopId) throws BusinessException {
        shop360Svc.deleteShop360Floors(shopId);
    }

    @ApiOperation(value = "Delete shop 360 floor", nickname = "delete360sFloor")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "Deleted"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @DeleteMapping(value = "/floor")
    public void deleteShop360Floor(@RequestHeader("User-Token") String userToken,
                                   @RequestParam("shop_id") Long shopId,
                                   @RequestParam("floor_id") Long floorId) throws BusinessException {
        shop360Svc.deleteShop360Floor(shopId, floorId);
    }

    @ApiOperation(value = "Delete shop 360 section", nickname = "delete360sSection")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "Deleted"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @DeleteMapping(value = "/section")
    public void deleteShop360Section(@RequestHeader("User-Token") String userToken,
                                     @RequestParam("shop_id") Long shopId,
                                     @RequestParam("section_id") Long sectionId) throws BusinessException {
        shop360Svc.deleteShop360Section(shopId, sectionId);
    }

    @ApiOperation(value = "Delete shop 360 scene", nickname = "delete360sScene")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "Deleted"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @DeleteMapping(value = "/scene")
    public void deleteShop360Scene(@RequestHeader("User-Token") String userToken,
                                   @RequestParam("shop_id") Long shopId,
                                   @RequestParam("scene_id") Long sceneId) throws BusinessException {
        shop360Svc.deleteShop360Scene(shopId, sceneId);
    }
}
