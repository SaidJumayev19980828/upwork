package com.nasnav.controller;

import com.nasnav.dto.response.AdvertisementDTO;
import com.nasnav.service.AdvertisementService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/advertisement")
@CrossOrigin("*")
public class AdvertisementController {
    @Autowired
    private AdvertisementService advertisementService;

    @GetMapping("")
    public PageImpl<AdvertisementDTO> getAllAdvertisements(@RequestHeader(name = "User-Token", required = false) String token,
                                                           @RequestParam(required = false, defaultValue = "0") Integer start,
                                                           @RequestParam(required = false, defaultValue = "10") Integer count) {
        return advertisementService.findAllAdvertisements(start, count);
    }

    @PostMapping("")
    public AdvertisementDTO createAdvertisement(@RequestHeader(name = "User-Token", required = false) String token,
                                                @RequestBody AdvertisementDTO advertisementDTO) {
        return advertisementService.create(advertisementDTO);
    }
}
