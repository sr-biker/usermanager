package com.digitalservicing.usermanager.exception;

public class UserNotFoundException extends RuntimeException {
    public UserNotFoundException() {
        super("No user found");
    }
}
