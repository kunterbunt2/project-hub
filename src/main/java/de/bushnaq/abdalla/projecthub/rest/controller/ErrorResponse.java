package de.bushnaq.abdalla.projecthub.rest.controller;

public class ErrorResponse {
    private final String message;
    private final int    status;

    public ErrorResponse(int status, String message) {
        this.status  = status;
        this.message = message;
    }

    // getters and setters
}