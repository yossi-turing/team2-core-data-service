package com.therapy.coredataservice.exception;

/** אימייל או סיסמה שגויים בהתחברות (מומר ל-401 Unauthorized). */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
