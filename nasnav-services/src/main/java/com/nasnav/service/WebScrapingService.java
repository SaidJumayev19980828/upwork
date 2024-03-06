package com.nasnav.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.nasnav.dto.WebScrapingRequest;
import com.nasnav.enumerations.ScrapingTypes;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.persistence.WebScrapingLog;
import com.nasnav.response.GenerateOrganizationPannerResponse;
import org.springframework.data.domain.PageImpl;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.sql.SQLException;

public interface WebScrapingService {

    void scrapeDataFromUrl(WebScrapingRequest request) throws JsonProcessingException;

	GenerateOrganizationPannerResponse callAIImageGenerator(Long orgId, String imageDescription, String oldPath) throws IOException, BusinessException;

    WebScrapingLog scrapeDataFromFile(Boolean manualCollect, String bootName ,Long orgId, MultipartFile file) throws IOException, BusinessException, SQLException, IllegalAccessException, InvocationTargetException;

	PageImpl<WebScrapingLog> getScrapingLogs(int start, int count, Long orgId, ScrapingTypes type);

	void deleteScrapingLog(Long id);
}
