package com.lahutina.phonecontacts.exception;

public class ContactNameExistsException extends RuntimeException {

    public ContactNameExistsException() {
        super();
    }

    public ContactNameExistsException(String message) {
        super(message);
    }
}
