package com.nasnav.controller;

import com.nasnav.dto.ShopJsonDTO;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.response.ResponseStatus;
import com.nasnav.response.ShopResponse;
import com.nasnav.service.EmployeeUserService;
import com.nasnav.service.ShopService;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RestController
@RequestMapping("/shop")
public class ShopsController {

    @Autowired
    private ShopService shopService;

    @Autowired
    private EmployeeUserService employeeUserService;

    @ApiOperation(value = "update information about shop by its ID or create a New shop", nickname = "updateShop")
    @ApiResponses(value = { @io.swagger.annotations.ApiResponse(code = 200, message = "OK"),
            @io.swagger.annotations.ApiResponse(code = 401, message = "INSUFFICIENT RIGHTS or UNAUTHENTICATED"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "INVALID_PARAM")})
    @PostMapping(value = "/update", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity updateShop(@RequestHeader(name = "User-ID") Long loggedUserId,
                                      @RequestHeader(name = "User-Token") String userToken,
                                      @RequestBody ShopJsonDTO shopJson) throws BusinessException {
        ShopResponse response;
        if (shopJson.getId() == null){
            response = shopService.createShop(loggedUserId, shopJson);
        } else {
            response = shopService.updateShop(loggedUserId, shopJson);
        }
        return new ResponseEntity(response, response.getHttpStatus());
    }
}
