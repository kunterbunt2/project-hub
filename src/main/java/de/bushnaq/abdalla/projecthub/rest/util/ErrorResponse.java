package de.bushnaq.abdalla.projecthub.rest.util;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ErrorResponse {
    private Exception exception;
    private String    message;
    private int       status;

    public ErrorResponse() {
    }

    public ErrorResponse(int status, String message, Exception e) {
        this.status    = status;
        this.message   = message;
        this.exception = e;
    }

}