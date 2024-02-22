package com.nasnav.integration.smsMis;

import com.nasnav.integration.MobileOTPService;
import com.nasnav.integration.smsMis.dto.OTPDto;
import com.nasnav.integration.smsMis.dto.OTPResponse;
import com.nasnav.service.impl.FileServiceImpl;
import lombok.RequiredArgsConstructor;
import org.jboss.logging.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;


@Service
@RequiredArgsConstructor
public class SmsMisrMobileOTPService implements MobileOTPService {
    private static Logger logger = Logger.getLogger(SmsMisrMobileOTPService.class);

    @Value("${smsmisr.endpoint}")
    private String endpoint;

    @Value("${smsmisr.environment}")
    private String environment;

    @Value("${smsmisr.userName}")
    private String userName;

    @Value("${smsmisr.password}")
    private String password;

    @Value("${smsmisr.sender}")
    private String sender;

    @Value("${smsmisr.template}")
    private String template;

    @Value("${smsmisr.language}")
    private String language;


    private final RestTemplate restTemplate;

    @Override
    public String send(OTPDto otpDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OTPResponse> requestBody = new HttpEntity<>(headers);
        OTPResponse response = restTemplate.postForObject(buildUrlParameters(otpDto), requestBody, OTPResponse.class);

        if(response.code().equals("1901")) {
            return "Success";
        }
        return null;
    }

    private URI buildUrlParameters(OTPDto otpDto){
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("environment", environment)
                .queryParam("username" , userName)
                .queryParam("password" , password)
                .queryParam("sender" , sender)
                .queryParam("mobile" , otpDto.mobile())
               // .queryParam("template", template)
                .queryParam("language", language)
                .queryParam("message" , otpDto.message());

        return builder.build().encode().toUri();
    }


}
