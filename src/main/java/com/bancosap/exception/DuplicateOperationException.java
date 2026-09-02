package com.bancosap.exception;

public class DuplicateOperationException extends RuntimeException {
    public DuplicateOperationException(String message) {
        super(message);
    }
}
