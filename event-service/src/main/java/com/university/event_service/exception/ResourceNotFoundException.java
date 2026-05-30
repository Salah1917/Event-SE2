package com.university.event_service.exception;

public class ResourceNotFoundException extends RuntimeException {

    private final Object id;

    public ResourceNotFoundException(String message, Object id) {
        super(message);
        this.id = id;
    }

    public Object getId() {
        return id;
    }
}
