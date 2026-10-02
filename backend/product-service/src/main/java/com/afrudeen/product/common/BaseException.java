package com.afrudeen.product.common;
import org.springframework.http.HttpStatus;
public abstract class BaseException extends RuntimeException {
    protected BaseException(String message) {
        super(message);
    }
    /** Each concrete exception decides its own HTTP status. */
    public abstract HttpStatus getStatus();
}