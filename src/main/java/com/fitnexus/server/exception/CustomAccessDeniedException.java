package com.fitnexus.server.exception;

import lombok.*;
import org.springframework.security.access.AccessDeniedException;


@Getter
@Setter
@ToString
public class CustomAccessDeniedException extends AccessDeniedException {

    private int status;
    private String message;

    public CustomAccessDeniedException(int status, String message) {
        super(message);
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}
