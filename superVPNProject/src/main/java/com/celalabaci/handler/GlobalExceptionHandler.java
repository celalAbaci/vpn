package com.celalabaci.handler;

import com.celalabaci.exception.BaseException;
import com.celalabaci.exception.ErrorResponse;
import com.celalabaci.exception.RateLimitExceededException; // YENİ IMPORT
import org.springframework.http.HttpHeaders; // YENİ IMPORT
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.time.OffsetDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BaseException.class)
    public ResponseEntity<ErrorResponse> handleBaseException(BaseException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.BAD_REQUEST.value(),
                ex.getMessageType().getCode() + ": " + ex.getMessageType().getMessage(),
                ex.getDetail(),
                request.getDescription(false).substring(4) // "uri=" kısmını kaldırır
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.BAD_REQUEST);
    }

    /**
     * YENİ EKLENDİ: RateLimitExceededException'ı yakalamak için özel handler.
     * Bu handler, standart BaseException'dan farklı olarak 429 (Too Many Requests)
     * HTTP durum kodunu ve "Retry-After" başlığını döner.
     */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<ErrorResponse> handleRateLimitExceededException(RateLimitExceededException ex, WebRequest request) {
        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.TOO_MANY_REQUESTS.value(),
                ex.getMessageType().getCode() + ": " + ex.getMessageType().getMessage(),
                ex.getDetail(),
                request.getDescription(false).substring(4) // "uri="
        );

        // İstemciye ne zaman tekrar denemesi gerektiğini bildiren HTTP başlığı
        HttpHeaders headers = new HttpHeaders();
        headers.add("Retry-After", String.valueOf(ex.getRetryAfterSeconds()));

        return new ResponseEntity<>(errorResponse, headers, HttpStatus.TOO_MANY_REQUESTS);
    }


    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(Exception ex, WebRequest request) {
        // Beklenmeyen hatalar için loglama eklemek iyi bir pratik olabilir
        // log.error("Beklenmeyen bir hata oluştu: ", ex);

        ErrorResponse errorResponse = new ErrorResponse(
                OffsetDateTime.now(),
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal Server Error",
                ex.getMessage(),
                request.getDescription(false).substring(4)
        );
        return new ResponseEntity<>(errorResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
