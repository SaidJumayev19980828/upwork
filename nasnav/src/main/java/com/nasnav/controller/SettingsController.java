package com.nasnav.controller;

import com.nasnav.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/frontend")
public class SettingsController {
    @Autowired
    private SettingService settingService;

    @GetMapping(value = "/setting", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getFrontEndSettings(@RequestParam("frontend_id") String frontendId){
        return settingService.frontEndSettings(frontendId);
    }
}
