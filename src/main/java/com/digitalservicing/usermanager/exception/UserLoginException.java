package com.digitalservicing.usermanager.exception;

public class UserLoginException extends RuntimeException {

    public UserLoginException() {
        super("Invalid username or password");
    }

}
