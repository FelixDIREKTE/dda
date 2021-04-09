package com.dd.dda.model.exception;

public class DataNotReleaseYet extends RuntimeException {
    public DataNotReleaseYet() {
        super();
    }

    public DataNotReleaseYet(String message) {
        super(message);
    }

    public DataNotReleaseYet(String message, Throwable cause) {
        super(message, cause);
    }

    public DataNotReleaseYet(Throwable cause) {
        super(cause);
    }

    public DataNotReleaseYet(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
