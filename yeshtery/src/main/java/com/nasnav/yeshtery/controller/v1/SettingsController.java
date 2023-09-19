package com.nasnav.yeshtery.controller.v1;

import com.nasnav.commons.YeshteryConstants;
import com.nasnav.service.SettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping(SettingsController.API_PATH)
@CrossOrigin("*")
public class SettingsController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/frontend";
    @Autowired
    private SettingService settingService;

    @GetMapping(value = "/setting", produces = MediaType.APPLICATION_JSON_VALUE)
    public Object getFrontEndSettings(@RequestParam("frontend_id") String frontendId) {
        return settingService.frontEndSettings(frontendId);
    }
}
