package com.example.facultyload.exception;

public class ApiException extends RuntimeException {
    public ApiException(String message) {
        super(message);
    }
}

