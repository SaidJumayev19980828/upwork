package com.nasnav.integration.smsmisr;

import com.nasnav.integration.MobileOTPService;
import com.nasnav.integration.smsmisr.dto.OTPDto;
import com.nasnav.integration.smsmisr.dto.OTPResponse;
import lombok.RequiredArgsConstructor;
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

    private final RestTemplate restTemplate;

    @Override
    public String send(OTPDto otpDto) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<OTPResponse> requestBody = new HttpEntity<>(headers);
        OTPResponse response = restTemplate.postForObject(buildUrlParameters(otpDto), requestBody, OTPResponse.class);

        if("4901".equals(response.code())) {
            return "Success";
        }
        return "Fail";
    }

    private URI buildUrlParameters(OTPDto otpDto){
        UriComponentsBuilder builder = UriComponentsBuilder.fromUriString(endpoint)
                .queryParam("environment", environment)
                .queryParam("username" , userName)
                .queryParam("password" , password)
                .queryParam("sender" , sender)
                .queryParam("mobile" , otpDto.mobile())
                .queryParam("template", template)
                .queryParam("otp" , otpDto.otp());

        return builder.build().encode().toUri();
    }


}
