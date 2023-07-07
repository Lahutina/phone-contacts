package com.lahutina.phonecontacts.service;

public interface ValidatorService {
    boolean validateEmail(String email);

    boolean validatePhoneNumber(String phoneNumber);
}
