package com.lahutina.phonecontacts.service;


import com.lahutina.phonecontacts.entity.dto.LoginDto;
import com.lahutina.phonecontacts.entity.dto.RegisterDto;

public interface AuthService {
    String login(LoginDto loginDto);

    String register(RegisterDto registerDto);
}
