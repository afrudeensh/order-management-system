package com.afrudeen.user.common;

import org.springframework.http.HttpStatus;

public class ServiceUnavailableException extends BaseException {
    public ServiceUnavailableException(String message) { super(message); }
    @Override
    public HttpStatus getStatus() { return HttpStatus.SERVICE_UNAVAILABLE; } // 503
}