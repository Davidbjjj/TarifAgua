package com.tarifaria.tabelaAgua.exception;

public class MethodNotAllowedException extends RuntimeException {

    public MethodNotAllowedException() {
        super();
    }

    public MethodNotAllowedException(String message) {
        super(message);
    }

    public MethodNotAllowedException(String message, Throwable cause) {
        super(message, cause);
    }
}

