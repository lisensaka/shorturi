package com.lhind.shorturi.exceptions;

public class JwtCustomException extends RuntimeException {

    public JwtCustomException(String message) {
        super(message);
    }
}

