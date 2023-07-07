package com.lahutina.phonecontacts.service.impl;

import com.lahutina.phonecontacts.service.ValidatorService;
import lombok.RequiredArgsConstructor;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class AbstractAPIValidatorService implements ValidatorService {

    private final RestTemplate restTemplate;
    @Value("${validation.key.phone}")
    private String apiKeyPhone;
    @Value("${validation.key.email}")
    private String apiKeyEmail;

    public boolean validateEmail(String email) {
        try {
            Thread.sleep(1000); // this service in free plan allows only 1 request per second
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String url = "https://emailvalidation.abstractapi.com/v1/";
        String fullUrl = url + "?api_key=" + apiKeyEmail + "&email=" + email;

        String response = restTemplate.getForEntity(fullUrl, String.class).getBody();

        if (response != null) {
            JSONObject jsonObject = new JSONObject(response);

            String deliverability = jsonObject.getString("deliverability");
            boolean isValidFormat = jsonObject.getJSONObject("is_valid_format").getBoolean("value");

            return "DELIVERABLE".equals(deliverability) && isValidFormat;
        }

        return false;
    }

    public boolean validatePhoneNumber(String phoneNumber) {
        try {
            Thread.sleep(1000); // this service in free plan allows only 1 request per second
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        String url = "https://phonevalidation.abstractapi.com/v1/";
        String fullUrl = url + "?api_key=" + apiKeyPhone + "&phone=" + phoneNumber;

        String response = restTemplate.getForEntity(fullUrl, String.class).getBody();

        if (response != null) {
            JSONObject jsonObject = new JSONObject(response);

            return jsonObject.getBoolean("valid");
        }

        return false;
    }
}