package com.afrudeen.user.common;
import org.springframework.http.HttpStatus;
public class ResourceNotFoundException extends BaseException {
    public ResourceNotFoundException(String message) { super(message); }
    @Override
    public HttpStatus getStatus() { return HttpStatus.NOT_FOUND; } // 404
}