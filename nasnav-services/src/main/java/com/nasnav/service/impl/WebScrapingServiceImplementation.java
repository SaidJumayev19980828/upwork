package com.nasnav.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nasnav.commons.utils.CustomOffsetAndLimitPageRequest;
import com.nasnav.dao.OrganizationRepository;
import com.nasnav.dao.WebScrapingLogRepository;
import com.nasnav.dto.WebScrapingFailure;
import com.nasnav.dto.WebScrapingRequest;
import com.nasnav.dto.WebScrapingResponse;
import com.nasnav.dto.request.notification.PushMessageDTO;
import com.nasnav.enumerations.ScrapingTypes;
import com.nasnav.exceptions.BusinessException;
import com.nasnav.exceptions.RuntimeBusinessException;
import com.nasnav.persistence.OrganizationEntity;
import com.nasnav.persistence.ShopsEntity;
import com.nasnav.persistence.WebScrapingLog;
import com.nasnav.response.GenerateOrganizationPannerResponse;
import com.nasnav.service.*;
import com.nasnav.service.notification.NotificationService;
import com.nasnav.util.MultipartFileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URI;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.time.ZonedDateTime;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.nasnav.enumerations.NotificationType.SCRAPPING_RESPONSE;
import static com.nasnav.exceptions.ErrorCodes.*;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_ACCEPTABLE;
import static org.springframework.http.HttpStatus.NOT_FOUND;

/**
 * The type Web scraping service implementation.
 */
@Service
@RequiredArgsConstructor
@Log4j2
public class WebScrapingServiceImplementation implements WebScrapingService {

	private final  OrganizationRepository organizationRepository;
    private final WebScrapingLogRepository scrapingLogRepository;
    private final OrganizationService organizationService;
    @Qualifier("csv")
    @Autowired
    private  CsvExcelDataExportService csvExportService;
    private final SecurityService security;
    private final NotificationService notificationService;
	private final FileService fileService;
    @Value("${ai-server}")
    private String serverUrl;

    @Value("${ai-key}")
    private String apiKey;

    /**
     * This method is responsible for scraping data from a given URL. It calls an AI service API with the provided URL and organization ID, and saves the response as a log entry in the database.
     * Async used here because AI request take almost 4 minute, so we can't let user wait for that Time.
     */
    @Override
    @Async
    public void scrapeDataFromUrl(WebScrapingRequest request) throws JsonProcessingException {
        String requestUrl = serverUrl + "/uploaderWebData";
        WebScrapingLog scrapingLog = callAIService(request, requestUrl);
        scrapingLogRepository.save(completeWebScrapingLog(scrapingLog,ScrapingTypes.URL_BASED, getOrganization(request.getOrganizationId()) , request.getUrl()));
        notifyOrganization(request.getOrganizationId(), scrapingLog.getLogMessage());
    }

    @Override
    public WebScrapingLog scrapeDataFromFile(Boolean manualCollect, String bootName,Long orgId, MultipartFile file) throws IOException, BusinessException, SQLException, IllegalAccessException, InvocationTargetException {
        validateInput(manualCollect, file);
        byte[] scrapFile = getScrapFileBytes(manualCollect, file);
        OrganizationEntity organization = getOrganization(orgId);
        String fileName = getFileName(manualCollect, file);
        return callAIForFile(scrapFile, prepareUrl(serverUrl , bootName ,organization ), organization , fileName);
    }

    @Override
    public PageImpl<WebScrapingLog> getScrapingLogs(int start, int count, Long orgId, ScrapingTypes type) {
        Pageable page = new CustomOffsetAndLimitPageRequest(start, count);
        return scrapingLogRepository.findAllByOrganizationAndLogTypeOrderByCreatedAtDesc(getOrganization(orgId), type, page);
    }

    @Override
    public void deleteScrapingLog(Long id) {
        WebScrapingLog  scrapingLog = scrapingLogRepository.findById(id).orElseThrow(() -> new RuntimeBusinessException(HttpStatus.NOT_FOUND,SCRAPPING$002,id));
        scrapingLogRepository.delete(scrapingLog);
    }

    private String prepareUrl(String endPoint, String chatBot, OrganizationEntity organization) {
        Long orgId = organization.getId();
        String botName = URLEncoder.encode(chatBot, StandardCharsets.UTF_8);
        String locations = addressesDataURL(organization);
        String aboutUs = URLEncoder.encode(organization.getDescription(), StandardCharsets.UTF_8);
      String deliveryArea = encoderURL("Delivery is only to the following zones ( Cairo, Giza,  Sharm E lShek and Zagazig ) Only");

        String retPolicy = encoderURL("Return in 14 days with invoice , Return  within 24 Hrs for fresh products");
        String contactUs = encoderURL("Phone Number is 201101219918 and our Call Center Hotline 19918 and oUr Complains Email is complains@tseppas.com and for any information our Email is info@tseppas.com and find our contacts on https://tseppas.com/contact-us");

        return String.format("%s/uploaderChatFiles?orgid=%s&botName=%s&aboutUs=%s&locations=%s&deliveryArea=%s&contactUs=%s&retPolicy=%s", endPoint, orgId, botName,aboutUs, locations,deliveryArea,contactUs,retPolicy);
    }

	@Override
	public GenerateOrganizationPannerResponse callAIImageGenerator(Long orgId, String imageDescription, String oldPath)
			throws BusinessException, IOException
	{
		String requestUrl = serverUrl + "/imagegenerate";
		if (organizationRepository.existsById(orgId))
		{
			if (oldPath != null)
			{
				fileService.deleteOrganizationFile(oldPath);
			}
			String imageBase64Code = callAIImageGenerator(requestUrl, orgId, imageDescription);
			MultipartFile orgBanner = MultipartFileUtils.convert(imageBase64Code, "banner.png", "image/png");
			String imageURL = fileService.saveFile(orgBanner, orgId);
			GenerateOrganizationPannerResponse response = new GenerateOrganizationPannerResponse();
			response.setOrganizationId(orgId);
			response.setImageUrl(imageURL);
			return response;
		}
		else
		{
			throw new RuntimeBusinessException(NOT_FOUND, ORG$NOTFOUND, orgId);
		}
	}

	private String callAIImageGenerator(String apiUrl, Long organizationId, String imageDescription) throws BusinessException
	{
		HttpHeaders headers = new HttpHeaders();
		headers.set("x-api-key", apiKey);
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<?> json = new HttpEntity<>(headers);
		String encodedMultiWordString = UriComponentsBuilder.newInstance().queryParam("chattxt", imageDescription).build().encode().toUriString();
		String urlWithParams = apiUrl + "?orgid=" + organizationId + "&chattxt=" + encodedMultiWordString;
		ResponseEntity<Map> responseEntity = new RestTemplate().exchange(URI.create(urlWithParams), HttpMethod.GET, json, Map.class);
		Map<String, String> response = responseEntity.getBody();
		int statusCode = responseEntity.getStatusCodeValue();
		if (statusCode == HttpStatus.OK.value())
		{
			return response.get("Encoded Data");
		}
		else
		{
			throw new BusinessException("INVALID PARAM: description", "Provided description empty on can not understood", NOT_ACCEPTABLE);
		}
	}

    private WebScrapingLog callAIForFile(byte[] file, String apiUrl ,OrganizationEntity organization ,String fileName) throws IOException {

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        headers.set("x-api-key", apiKey);

        MultiValueMap<String, String> fileMap = new LinkedMultiValueMap<>();
        ContentDisposition contentDisposition = ContentDisposition
                .builder("form-data")
                .name("file")
                .filename("Master_File.csv")
                .build();
        fileMap.add(HttpHeaders.CONTENT_DISPOSITION, contentDisposition.toString());
        HttpEntity<byte[]> fileEntity = new HttpEntity<>(file, fileMap);

        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("file", fileEntity);

        HttpEntity<MultiValueMap<String, Object>> requestEntity =
                new HttpEntity<>(body, headers);
        RestTemplate restTemplate = new RestTemplate();
        try {
            ResponseEntity<String> responseEntity = restTemplate
                    .postForEntity(apiUrl, requestEntity, String.class);
            return processResponse(responseEntity,organization,fileName);
        } catch (Exception e) {
            failureScraping(e.getMessage(),organization,fileName);
            throw new RuntimeBusinessException(INTERNAL_SERVER_ERROR , SCRAPPING$003 , e.getMessage());
        }
    }


    private WebScrapingLog processResponse(ResponseEntity<String> responseEntity, OrganizationEntity organization, String fileName) throws JsonProcessingException {
        String response = responseEntity.getBody();
        int statusCode = responseEntity.getStatusCodeValue();
        if (!responseEntity.getStatusCode().is2xxSuccessful()) {
            saveLog(handleFailureCase(response, statusCode),organization,fileName);
        }
        return saveLog(handleSuccess(response, statusCode), organization, fileName);

    }

    private void failureScraping(String exception,OrganizationEntity organization , String fileName){
        log.error("Exception while calling AI service: " + exception);
        WebScrapingLog failedLog = buildWebScrapping(exception, "Exception while calling AI service", HttpStatus.INTERNAL_SERVER_ERROR.value());
        saveLog(failedLog, organization, fileName);
    }

    private  WebScrapingLog saveLog( WebScrapingLog scrapingLog , OrganizationEntity organization , String fileName ){
       return scrapingLogRepository.save(completeWebScrapingLog(scrapingLog, ScrapingTypes.FILE_BASED, organization, fileName));
    }

    private OrganizationEntity getOrganization(Long orgId) {
       return organizationService.getOrganizationById(orgId);
    }

    private WebScrapingLog callAIService(WebScrapingRequest request, String apiUrl) throws JsonProcessingException {
        HttpHeaders headers = new HttpHeaders();
        headers.set("x-api-key", apiKey);
        String urlWithParams = apiUrl + "?orgid=" + request.getOrganizationId() + "&user_url=" + request.getUrl();

        HttpEntity<WebScrapingRequest> requestEntity = new HttpEntity<>(request, headers);
        try {
            ResponseEntity<String> responseEntity = new RestTemplate().exchange(
                    URI.create(urlWithParams),
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            String response = responseEntity.getBody();
            int statusCode = responseEntity.getStatusCodeValue();

             return (statusCode == HttpStatus.OK.value()) ?
                    handleSuccess(response, statusCode) :
                    handleFailureCase(response, statusCode);
        } catch (Exception e) {
            log.error("Exception while calling AI service: " + e.getMessage());
            return buildWebScrapping(e.getMessage() , "Exception while calling AI service" , HttpStatus.INTERNAL_SERVER_ERROR.value());
        }
    }

    private WebScrapingLog handleFailureCase(String error, int statusCode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        WebScrapingFailure failure = objectMapper.readValue(error, WebScrapingFailure.class);

        return buildWebScrapping(failure.getMessage() , failure.getError() , statusCode);
    }

    private WebScrapingLog handleSuccess(String body, int statusCode) throws JsonProcessingException {
        ObjectMapper objectMapper = new ObjectMapper();
        WebScrapingResponse response = objectMapper.readValue(body, WebScrapingResponse.class);

        return buildWebScrapping(response.getMessage() , response.getStatuCode() ,statusCode);

    }
    private WebScrapingLog completeWebScrapingLog(WebScrapingLog  scrapingLog, ScrapingTypes type ,OrganizationEntity organization , String url ) {
        scrapingLog.setRequestUrl(url);
        scrapingLog.setOrganization(organization);
        scrapingLog.setLogType(type);
        scrapingLog.setCreatedAt(ZonedDateTime.now());
        return scrapingLog;
    }

    private void notifyOrganization(Long orgId, String response) {
        notificationService.sendMessageToOrganizationEmplyees(
                orgId,
                new PushMessageDTO<>("Your Scrapping Response", response, SCRAPPING_RESPONSE)
        );
    }

    private void validateInput(Boolean manualCollect, MultipartFile file) {
        if (manualCollect && file == null) {
            throw new RuntimeBusinessException(HttpStatus.BAD_REQUEST, SCRAPPING$001);
        }
    }

    private byte[] getScrapFileBytes(Boolean manualCollect, MultipartFile file) throws IOException, BusinessException, SQLException, InvocationTargetException, IllegalAccessException {
        if (manualCollect) {
            return file.getBytes();
        } else {
            ByteArrayOutputStream outputStream = csvExportService.generateProductsFile(null, false);
            return outputStream.toByteArray();
        }
    }
    private String getFileName(Boolean manualCollect, MultipartFile file) {
        return manualCollect ? file.getOriginalFilename() : "products.csv";
    }
    private String addressesDataURL(OrganizationEntity organization){
       String addressesData =  organization.getShops().stream()
                .map(ShopsEntity::getAddressesEntity)
                .filter(Objects::nonNull)
                .map(addressesEntity -> String.format("Name: %s, LastName: %s, Flat Number: %s, Building Number: %s, " +
                                "Address Line 1: %s, Address Line 2: %s, Latitude: %s, Longitude: %s, " +
                                "Phone Number: %s, Postal Code: %s",
                        addressesEntity.getFirstName(), addressesEntity.getLastName(),
                        addressesEntity.getFlatNumber(), addressesEntity.getBuildingNumber(),
                        addressesEntity.getAddressLine1(), addressesEntity.getAddressLine2(),
                        addressesEntity.getLatitude(), addressesEntity.getLongitude(),
                        addressesEntity.getPhoneNumber(), addressesEntity.getPostalCode()))
                .collect(Collectors.joining(","));
        return  encoderURL(addressesData);

    }

    private String encoderURL(String data){
      return   URLEncoder.encode(data, StandardCharsets.UTF_8);
    }


    private WebScrapingLog buildWebScrapping(String logMessage, String requestStatus , int httpStatusCode ) {
        return   WebScrapingLog.builder()
                .logMessage(logMessage)
                .statusMessage(requestStatus)
                .httpStatusCode(httpStatusCode)
                .build();
    }

}
