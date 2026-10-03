package com.afrudeen.notification.common;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.LinkedHashMap;
import java.util.Map;


@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

        @ExceptionHandler(BaseException.class)
        public ResponseEntity<BaseResponse<Void>> handleBase(BaseException ex, HttpServletRequest req) {
            return build(ex.getStatus(), ex.getMessage(), req, null);
        }

        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<BaseResponse<Void>> handleValidation(MethodArgumentNotValidException ex,
                                                                   HttpServletRequest req) {
            Map<String, String> fields = new LinkedHashMap<>();
            ex.getBindingResult().getFieldErrors()
                    .forEach(fe -> fields.put(fe.getField(), fe.getDefaultMessage()));
            return build(HttpStatus.BAD_REQUEST, "Validation failed", req, fields);
        }

        @ExceptionHandler({HttpMessageNotReadableException.class,
                MethodArgumentTypeMismatchException.class})
        public ResponseEntity<BaseResponse<Void>> handleBadInput(Exception ex, HttpServletRequest req) {
            return build(HttpStatus.BAD_REQUEST, "Malformed request", req, null);
        }

        @ExceptionHandler(Exception.class)
        public ResponseEntity<BaseResponse<Void>> handleOther(Exception ex, HttpServletRequest req) {
            log.error("Unhandled error on {}", req.getRequestURI(), ex);
            return build(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, null);
        }

        private ResponseEntity<BaseResponse<Void>> build(HttpStatus status, String msg,
                                                         HttpServletRequest req,
                                                         Map<String, String> fields) {
            return ResponseEntity.status(status)
                    .body(BaseResponse.error(status.value(), msg, req.getRequestURI(), fields));
        }
}