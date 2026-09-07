package com.keepguard.ms_company.domain.exception;

public class InvalidStatusForOperationException extends RuntimeException {

    public InvalidStatusForOperationException(String message) {
        super(message);
    }
}
