package com.bucott.taskmanager.exception;

public class UsernameOrEmailNotFoundException extends RuntimeException {
    public UsernameOrEmailNotFoundException(String message) {
        super(message);
    }


}
