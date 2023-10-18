package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(AdvertisementController.API_PATH)
@CrossOrigin("*")
public class AdvertisementController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/advertisement";
    @Autowired
    private AdvertisementService advertisementService;

    @GetMapping("")
    public PageImpl<AdvertisementDTO> getAllAdvertisements(@RequestHeader(name = "User-Token", required = false) String token,
                                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                                           @RequestParam(required = false, defaultValue = "10") Integer count,
                                                           @RequestParam(required = false) String orgId) {
        return advertisementService.findAllAdvertisements(orgId, start, count);
    }

    @GetMapping("/{id}")
    public ResponseEntity<AdvertisementDTO> getOneAdvertisementsById(@RequestHeader(name = "User-Token", required = false) String token,
                                                                     @PathVariable("id") Long id) {
        return advertisementService.findAdvertisementById(id).map(ResponseEntity::ok).orElse(ResponseEntity.noContent().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAdvertisementsById(@RequestHeader(name = "User-Token", required = false) String token,
                                                         @PathVariable("id") Long id) {
        advertisementService.deleteAdvertisementById(id);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("")
    public AdvertisementDTO createAdvertisement(@RequestHeader(name = "User-Token", required = false) String token,
                                                @Validated @RequestBody AdvertisementDTO advertisementDTO) {
        if (advertisementDTO.getId() == null) {
            return advertisementService.create(advertisementDTO);
        } else {
            advertisementService.update(advertisementDTO);
            return advertisementService.findAdvertisementById(advertisementDTO.getId()).orElse(null);
        }
    }
}
