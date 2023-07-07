package com.lahutina.phonecontacts.exception;

public class ContactNotFoundException extends RuntimeException {
    public ContactNotFoundException() {
        super();
    }

    public ContactNotFoundException(String message) {
        super(message);
    }
}