package com.dd.dda.model.exception;

public class DDAException extends RuntimeException {
    public DDAException() {
        super();
    }

    public DDAException(String message) {
        super(message);
    }

    public DDAException(String message, Throwable cause) {
        super(message, cause);
    }

    public DDAException(Throwable cause) {
        super(cause);
    }

    public DDAException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
