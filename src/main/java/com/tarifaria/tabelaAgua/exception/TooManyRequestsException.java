package com.tarifaria.tabelaAgua.exception;

public class TooManyRequestsException extends RuntimeException {

    private final int retryAfterSeconds;

    public TooManyRequestsException() {
        super();
        this.retryAfterSeconds = 60;
    }

    public TooManyRequestsException(String message) {
        super(message);
        this.retryAfterSeconds = 60;
    }

    public TooManyRequestsException(String message, int retryAfterSeconds) {
        super(message);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public TooManyRequestsException(String message, Throwable cause, int retryAfterSeconds) {
        super(message, cause);
        this.retryAfterSeconds = retryAfterSeconds;
    }

    public int getRetryAfterSeconds() {
        return retryAfterSeconds;
    }
}

