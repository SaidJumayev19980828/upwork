package com.nasnav.controller;


import com.nasnav.dto.CategoryRepresentationObject;
import com.nasnav.response.ApiResponseBuilder;
import com.nasnav.response.UserApiResponse;
import com.nasnav.service.CategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/admin")
@Api(description = "Set of endpoints for adding, updating and deleting Dashboard data.")
@CrossOrigin("*") // allow all origins
public class AdminController {

    @Autowired
    private CategoryService categoryService;

    public AdminController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @ApiOperation(value = "Create or update a Category", nickname = "categoryModification", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "create",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public ApiResponseBuilder createEmployeeUser(@RequestHeader (value = "User-ID", required = true) Long userId,
                                                 @RequestHeader (value = "User-Token", required = true) String userToken,
                                                 @RequestBody CategoryRepresentationObject categoryJson) {
        if (categoryJson.getId() != null && categoryJson.getOperation() == "update"){
            return categoryService.updateCategory(categoryJson);
        }
        return categoryService.createCategory(categoryJson);//this.employeeUserService.createEmployeeUser(userId, userToken, employeeUserJson);
    }
}
