package com.lahutina.phonecontacts.controller;

import com.lahutina.phonecontacts.entity.dto.LoginDto;
import com.lahutina.phonecontacts.entity.dto.RegisterDto;
import com.lahutina.phonecontacts.service.AuthService;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    public AuthControllerTest() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegister() {
        RegisterDto registerDto = new RegisterDto("testuser@gmail.com", "password", "password");

        when(authService.register(registerDto)).thenReturn("Registration successful");

        ResponseEntity<String> responseEntity = authController.register(registerDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Registration successful", responseEntity.getBody());

        verify(authService).register(registerDto);
    }

    @Test
    public void testLogin() {
        LoginDto loginDto = new LoginDto("testuser@gmail.com", "password");

        when(authService.login(loginDto)).thenReturn("Login successful");

        ResponseEntity<String> responseEntity = authController.login(loginDto);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertEquals("Login successful", responseEntity.getBody());

        verify(authService).login(loginDto);
    }
}
