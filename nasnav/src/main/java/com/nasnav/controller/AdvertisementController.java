package com.nasnav.controller;


import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/advertisement")
@CrossOrigin("*")
public class AdvertisementController {
    @Autowired
    private AdvertisementService advertisementService;

    @GetMapping("")
    public PageImpl<AdvertisementDTO> getAllAdvertisements(@RequestHeader(name = "User-Token", required = false) String token,
                                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                                           @RequestParam(required = false, defaultValue = "10") Integer count,
                                                           @RequestParam(required = false) Long orgId,
                                                           @RequestParam(required = false) String name,
                                                           @RequestParam(required = false, name = "fromDate")
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
                                                           @RequestParam(required = false, name = "toDate")
                                                           @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate) {
        return advertisementService.findAllAdvertisements(orgId, start, count, fromDate, toDate, name);
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
