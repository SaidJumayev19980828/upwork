package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.response.ThreeDModelList;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.request.ThreeDModelSearchParam;
import com.nasnav.service.ThreeDModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping("/product/model3d")
@CrossOrigin("*")
public class ThreeModelController {
    private final ThreeDModelService threeModelService;

    @Autowired
    public ThreeModelController(ThreeDModelService threeModelService) {
        this.threeModelService = threeModelService;
    }

    @PostMapping(produces = APPLICATION_JSON_VALUE, headers = "Content-Type= multipart/form-data", consumes = {"multipart/form-data"})
    public ThreeDModelResponse upload3DModelFiles(
            @RequestHeader (name = "User-Token", required = false) String userToken,
            @RequestPart("files") MultipartFile[] files,
            @RequestPart(value = "properties") String jsonString) throws JsonProcessingException {
        return threeModelService.createNewThreeModel(jsonString, files);
    }

    @PutMapping(value = "/{modelId}", produces = APPLICATION_JSON_VALUE, headers = "Content-Type= multipart/form-data", consumes = {"multipart/form-data"})
    public ThreeDModelResponse update3DModelFiles(
            @RequestHeader (name = "User-Token", required = false) String userToken,
            @PathVariable("modelId") Long modelId,
            @RequestPart("files") MultipartFile[] files,
            @RequestPart(value = "properties") String jsonString) throws JsonProcessingException {
        return threeModelService.updateThreeDModel(modelId, jsonString, files);
    }

    @GetMapping(value = "/one")
    public ThreeDModelResponse getThreeDModel(
            @RequestHeader (name = "User-Token", required = false) String userToken,
            @RequestParam(value = "barcode", required = false) String barcode,
            @RequestParam(value = "sku", required = false) String sku) {
        return threeModelService.getThreeDModelByBarcodeOrSKU(barcode, sku);
    }

    @GetMapping(value = "/all")
    public ThreeDModelList getThreeDModelAll(
            @RequestHeader (name = "User-Token", required = false) String userToken,
            @RequestParam(name = "name", required = false) String name,
            @RequestParam(name = "barcode", required = false) String barcode,
            @RequestParam(name = "sku", required = false) String sku,
            @RequestParam(name = "description", required = false) String description,
            @RequestParam(name = "color", required = false) String color,
            @RequestParam(name = "size", required = false) String size,
            @RequestParam(name = "start", required = false) Integer start,
            @RequestParam(name = "count", required = false) Integer count
    ) {
        ThreeDModelSearchParam searchParam = new ThreeDModelSearchParam(name, barcode, sku, description, color, size, start, count);
        return threeModelService.getThreeDModelAll(searchParam);
    }

    @PostMapping(value = "/assign")
    public void assign3DModelToProduct(
            @RequestHeader (name = "User-Token", required = false) String userToken,
            @RequestParam("model_id") Long modelId,
            @RequestParam("product_id") Long productId) {
        threeModelService.assignModelToProduct(modelId, productId);
    }

    @DeleteMapping(value = "/{modelId}")
    public void delete3DModel(
            @RequestHeader (name = "User-Token", required = false) String userToken,
            @PathVariable("modelId") Long modelId) {
        threeModelService.deleteThreeDModel(modelId);
    }

}
