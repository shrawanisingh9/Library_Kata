package org.example.exceptions;

public class UserNotFoundException extends LibraryException {

    public UserNotFoundException(String userId) {
        super("User not found: " + userId);
    }
}