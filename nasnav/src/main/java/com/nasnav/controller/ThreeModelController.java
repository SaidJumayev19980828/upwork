package com.nasnav.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.response.ThreeDModelResponse;
import com.nasnav.service.ThreeDModelService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;


@RestController
@RequestMapping("/product")
@CrossOrigin("*")
public class ThreeModelController {
  private final ThreeDModelService threeModelService;

    @Autowired
    public ThreeModelController(ThreeDModelService threeModelService) {
        this.threeModelService = threeModelService;
    }

    @PostMapping(value = "/add/new3d/model", produces = APPLICATION_JSON_VALUE,headers = "Content-Type= multipart/form-data", consumes = {"multipart/form-data"})
    public ThreeDModelResponse upload3DModelFiles(
                                    @RequestPart("files") MultipartFile[] files,
                                   @RequestPart(value = "properties") String jsonString ) throws JsonProcessingException {
     return threeModelService.createNewThreeModel(jsonString,files);
    }

    @GetMapping(value = "/get3d/model")
    public ThreeDModelResponse getThreeDModel(
                                              @RequestParam(value = "barcode",required = false) String barcode,
                                              @RequestParam(value = "sku",required = false) String sku){
        return threeModelService.getThreeDModelByBarcodeOrSKU(barcode,sku);
    }

    @PostMapping(value="/assign/model/to/product")
    public void assign3DModelToProduct(
                                       @RequestParam("model_id") Long modelId,
                                       @RequestParam("product_id") Long productId){
        threeModelService.assignModelToProduct(modelId,productId);

    }

}
