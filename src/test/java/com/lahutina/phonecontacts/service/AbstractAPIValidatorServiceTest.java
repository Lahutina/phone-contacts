package com.lahutina.phonecontacts.service;

import com.lahutina.phonecontacts.service.impl.AbstractAPIValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AbstractAPIValidatorServiceTest {

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private AbstractAPIValidatorService validatorService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        validatorService = new AbstractAPIValidatorService(restTemplate);
    }

    @Test
    void testValidateEmail_ValidEmail() {
        String email = "test@gmail.com";
        String mockResponse = "{\"deliverability\": \"DELIVERABLE\", \"is_valid_format\": {\"value\": true}}";
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        boolean isValid = validatorService.validateEmail(email);

        assertTrue(isValid);
        verify(restTemplate).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testValidateEmail_InvalidEmail() {
        String email = "invalid_email";
        String mockResponse = "{\"deliverability\": \"UNDELIVERABLE\", \"is_valid_format\": {\"value\": false}}";
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        boolean isValid = validatorService.validateEmail(email);

        assertFalse(isValid);
        verify(restTemplate).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testValidatePhoneNumber_ValidNumber() {
        String phoneNumber = "+380685144213";
        String mockResponse = "{\"valid\": true}";
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        boolean isValid = validatorService.validatePhoneNumber(phoneNumber);

        assertTrue(isValid);
        verify(restTemplate).getForEntity(anyString(), eq(String.class));
    }

    @Test
    void testValidatePhoneNumber_InvalidNumber() {
        String phoneNumber = "12345";
        String mockResponse = "{\"valid\": false}";
        when(restTemplate.getForEntity(anyString(), eq(String.class))).thenReturn(new ResponseEntity<>(mockResponse, HttpStatus.OK));

        boolean isValid = validatorService.validatePhoneNumber(phoneNumber);

        assertFalse(isValid);
        verify(restTemplate).getForEntity(anyString(), eq(String.class));
    }
}