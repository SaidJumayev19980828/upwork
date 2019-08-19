package com.nasnav.controller;


import com.nasnav.dto.CategoryDTO;
import com.nasnav.response.CategoryResponse;
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
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @PostMapping(value = "category",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponse createCategory(@RequestHeader (value = "User-ID", required = true) Long userId,
                                             @RequestHeader (value = "User-Token", required = true) String userToken,
                                             @RequestBody CategoryDTO.CategoryModificationObject categoryJson) {
        if (categoryJson.getOperation().equals("update")){
            return categoryService.updateCategory(categoryJson);
        }
        return categoryService.createCategory(categoryJson);
    }


    @ApiOperation(value = "delete a Category", nickname = "categoryDeletion", code = 200)
    @ApiResponses(value = {
            @io.swagger.annotations.ApiResponse(code = 200, message = "process completed successfully"),
            @io.swagger.annotations.ApiResponse(code = 403, message = "User not authorized to do this action"),
            @io.swagger.annotations.ApiResponse(code = 406, message = "Invalid or missing parameter"),
    })
    @DeleteMapping(value = "category",
            produces = MediaType.APPLICATION_JSON_UTF8_VALUE,
            consumes = MediaType.APPLICATION_JSON_UTF8_VALUE)
    @ResponseStatus(HttpStatus.OK)
    public CategoryResponse deleteCategory(@RequestHeader (value = "User-ID") Long userId,
                                           @RequestHeader (value = "User-Token") String userToken,
                                           @RequestParam (value = "category_id") Long categoryId ) {
        if (categoryId == null ){
            return new CategoryResponse("MISSING_PRARM: Category_id", "");
        }
        return categoryService.deleteCategory(categoryId);
    }
}
