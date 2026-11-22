package com.therapy.coredataservice.exception;

/** ניסיון הרשמה עם אימייל שכבר קיים (מומר ל-409 Conflict). */
public class EmailAlreadyRegisteredException extends RuntimeException {
    public EmailAlreadyRegisteredException(String message) {
        super(message);
    }
}
