package com.scholar.service;

/**
 * A user facing validation problem. The message is safe to show in a dialog.
 */
public class ValidationException extends Exception {

    public ValidationException(String message) {
        super(message);
    }
}
