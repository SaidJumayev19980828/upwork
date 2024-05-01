package com.nasnav.yeshtery.controller.v1;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.commons.YeshteryConstants;
import com.nasnav.dto.WebScrapingRequest;
import com.nasnav.enumerations.ScrapingTypes;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.WebScrapingLog;
import com.nasnav.service.WebScrapingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageImpl;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

import static com.nasnav.constatnts.DefaultValueStrings.DEFAULT_PAGING_COUNT;
import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

@RestController
@RequestMapping(value = WebScrapingController.API_PATH, produces = APPLICATION_JSON_VALUE)
@RequiredArgsConstructor
@Validated
public class WebScrapingController {
    static final String API_PATH = YeshteryConstants.API_PATH + "/scraping";
    private final WebScrapingService webScrapingService;



    @PostMapping
    public String scrapeDataFromUrl(@RequestHeader(name = "User-Token", required = false) String userToken, @Valid @RequestBody WebScrapingRequest scraping) throws JsonProcessingException {
         webScrapingService.scrapeDataFromUrl(scraping);
        return "Your web scraping request has been received and is being processed.";
    }

    @PostMapping("/file")
    public  WebScrapingLog scrapeData(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam("manualCollect") Boolean manualCollect,
            @RequestParam("bootName") String bootName,
            @RequestParam("orgId") Long orgId,
            @RequestParam(value = "file", required = false) MultipartFile file
    ) throws BusinessException, SQLException, IOException, InvocationTargetException, IllegalAccessException {
            return  webScrapingService.scrapeDataFromFile(manualCollect, bootName, orgId ,file);
    }

    @GetMapping
    public PageImpl<WebScrapingLog> getScrapingLogs(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam(required = false, defaultValue = "0") Integer start,
            @RequestParam(required = false, defaultValue = DEFAULT_PAGING_COUNT) Integer count,
            @RequestParam(required = false) Long orgId ,
            @RequestParam(required = false) ScrapingTypes type
    ) {
        return webScrapingService.getScrapingLogs(start,count,orgId,type);
    }

    @DeleteMapping
    public void deleteScrapingLog(
            @RequestHeader(name = "User-Token", required = false) String userToken,
            @RequestParam("id") Long id
    ) {
        webScrapingService.deleteScrapingLog(id);
    }
}
