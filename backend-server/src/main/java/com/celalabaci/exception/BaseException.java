package com.celalabaci.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@Getter
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BaseException extends RuntimeException {
    private final MessageType messageType;
    private final String detail;

    public BaseException(MessageType messageType, String detail) {
        super(messageType.getMessage());
        this.messageType = messageType;
        this.detail = detail;
    }
}