package com.toplms.core.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class UserNotFoundException extends RuntimeException {

    public UserNotFoundException(String message) {
        super(message);
    }

    // Helpful overloaded constructor for looking up by email or ID
    public static UserNotFoundException createWithIdentifier(String identifier) {
        return new UserNotFoundException(String.format("User not found with identifier: '%s'", identifier));
    }
}
