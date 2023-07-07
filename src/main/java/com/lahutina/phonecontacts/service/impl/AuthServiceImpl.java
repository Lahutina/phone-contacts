package com.lahutina.phonecontacts.service.impl;

import com.lahutina.phonecontacts.entity.User;
import com.lahutina.phonecontacts.entity.dto.LoginDto;
import com.lahutina.phonecontacts.entity.dto.RegisterDto;
import com.lahutina.phonecontacts.exception.InvalidCredentialsException;
import com.lahutina.phonecontacts.exception.UserAlreadyExistsException;
import com.lahutina.phonecontacts.exception.UserNotFoundException;
import com.lahutina.phonecontacts.repository.UserRepository;
import com.lahutina.phonecontacts.service.AuthService;
import com.lahutina.phonecontacts.service.JwtService;
import com.lahutina.phonecontacts.service.ValidatorService;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Service class for user login and registration.
 */
@Service
@AllArgsConstructor
public class AuthServiceImpl implements AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final ValidatorService validatorService;
    private final AuthenticationManager authenticationManager;

    // Performs user login by validating the credentials and generating a JWT token
    @Override
    public String login(LoginDto loginDto) {
        User user = userRepository.findByEmail(loginDto.login());
        if (user == null) {
            throw new UserNotFoundException("User not found");
        } else if (!passwordEncoder.matches(loginDto.password(), user.getPassword())) {
            throw new InvalidCredentialsException("Password is incorrect");
        }
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginDto.login(), loginDto.password()));

        return jwtService.generateToken(user);
    }

    // Registers a new user by creating a user record and generating a JWT token for him
    @Override
    public String register(RegisterDto registerDto) {
        if (userRepository.findByEmail(registerDto.login()) != null) {
            throw new UserAlreadyExistsException("User with this email already exists");
        } else if (!registerDto.password().equals(registerDto.repeatPassword())) {
            throw new InvalidCredentialsException("Passwords do not match");
        } else if (!validatorService.validateEmail(registerDto.login())) {
            throw new InvalidCredentialsException("Invalid email " + registerDto.login());
        }

        User user = new User();
        user.setEmail(registerDto.login());
        user.setPassword(passwordEncoder.encode(registerDto.password()));
        userRepository.save(user);

        return jwtService.generateToken(user);
    }
}
