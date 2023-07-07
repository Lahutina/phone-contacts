package com.lahutina.phonecontacts.service;

import com.lahutina.phonecontacts.entity.User;
import com.lahutina.phonecontacts.entity.dto.LoginDto;
import com.lahutina.phonecontacts.entity.dto.RegisterDto;
import com.lahutina.phonecontacts.exception.InvalidCredentialsException;
import com.lahutina.phonecontacts.exception.UserAlreadyExistsException;
import com.lahutina.phonecontacts.exception.UserNotFoundException;
import com.lahutina.phonecontacts.repository.UserRepository;
import com.lahutina.phonecontacts.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private ValidatorService validatorService;

    @Mock
    private AuthenticationManager authenticationManager;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        authService = new AuthServiceImpl(userRepository, passwordEncoder, jwtService, validatorService, authenticationManager);
    }

    @Test
    void testLogin_ValidCredentials() {
        String email = "oksankalahutina@gmail.com";
        String password = "password";
        String encodedPassword = "encodedPassword";
        String token = "token";

        LoginDto loginDto = new LoginDto(email, password);
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(true);
        when(jwtService.generateToken(user)).thenReturn(token);

        assertEquals(token, authService.login(loginDto));
        verify(authenticationManager).authenticate(new UsernamePasswordAuthenticationToken(email, password));
    }

    @Test
    void testLogin_UserNotFound() {
        String email = "test@example.com";
        String password = "password";

        LoginDto loginDto = new LoginDto(email, password);

        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(UserNotFoundException.class, () -> authService.login(loginDto));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testLogin_InvalidPassword() {
        String email = "test@example.com";
        String password = "password";
        String encodedPassword = "encodedPassword";

        LoginDto loginDto = new LoginDto(email, password);
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(user);
        when(passwordEncoder.matches(password, encodedPassword)).thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> authService.login(loginDto));
        verify(authenticationManager, never()).authenticate(any());
    }

    @Test
    void testRegister_ValidCredentials() {
        String email = "oksankalahutina@gmail.com";
        String password = "password";
        String repeatPassword = "password";
        String encodedPassword = "encodedPassword";
        String token = "token";

        RegisterDto registerDto = new RegisterDto(email, password, repeatPassword);
        User user = new User();
        user.setEmail(email);
        user.setPassword(encodedPassword);

        when(userRepository.findByEmail(email)).thenReturn(null);
        when(passwordEncoder.encode(password)).thenReturn(encodedPassword);
        when(jwtService.generateToken(user)).thenReturn(token);
        when(validatorService.validateEmail(email)).thenReturn(true);

        String result = authService.register(registerDto);

        assertEquals(token, result);
        verify(userRepository).save(user);
    }

    @Test
    void testRegister_UserAlreadyExists() {
        String email = "test@example.com";
        String password = "password";
        String repeatPassword = "password";

        RegisterDto registerDto = new RegisterDto(email, password, repeatPassword);

        when(userRepository.findByEmail(email)).thenReturn(new User());

        assertThrows(UserAlreadyExistsException.class, () -> authService.register(registerDto));
        verify(userRepository, never()).save(any());
    }

    @Test
    void testRegister_PasswordsDoNotMatch() {
        String email = "test@example.com";
        String password = "password";
        String repeatPassword = "differentPassword";

        RegisterDto registerDto = new RegisterDto(email, password, repeatPassword);

        when(userRepository.findByEmail(email)).thenReturn(null);

        assertThrows(InvalidCredentialsException.class, () -> authService.register(registerDto));
        verify(userRepository, never()).save(any());
    }
}