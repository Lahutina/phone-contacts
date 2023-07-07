package com.lahutina.phonecontacts.service;

import com.lahutina.phonecontacts.entity.User;
import com.lahutina.phonecontacts.service.impl.JwtServiceImpl;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;

import java.lang.reflect.Field;
import java.security.Key;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

public class JwtServiceTest {

    @InjectMocks
    private JwtServiceImpl jwtService;

    private UserDetails userDetails;

    @BeforeEach
    void setUp() throws NoSuchFieldException, IllegalAccessException {
        MockitoAnnotations.openMocks(this);
        jwtService = new JwtServiceImpl();
        userDetails = new User(1L, "username", "password", new ArrayList<>());
        Field field = jwtService.getClass().getDeclaredField("jwtSigningKey");
        field.setAccessible(true);
        field.set(jwtService, "secretsecretsecretsecretsecretsecretsecretsecret");
    }

    @Test
    void testExtractUserName() {
        String token = jwtService.generateToken(userDetails);
        String extractedUserName = jwtService.extractUserName(token);
        assertEquals(userDetails.getUsername(), extractedUserName);
    }

    @Test
    void testGenerateToken() {
        String token = jwtService.generateToken(userDetails);
        assertNotNull(token);
    }

    @Test
    void testIsTokenValid_ValidToken() {
        String token = jwtService.generateToken(userDetails);
        assertTrue(jwtService.isTokenValid(token, userDetails));
    }

    @Test
    void testGetSigningKey() {
        Key key = jwtService.getSigningKey();
        assertNotNull(key);
    }

    @Test
    void testExtractClaim() {
        String token = jwtService.generateToken(userDetails);
        String subject = jwtService.extractClaim(token, Claims::getSubject);
        assertEquals(userDetails.getUsername(), subject);
    }
}