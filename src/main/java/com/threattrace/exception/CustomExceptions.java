package com.threattrace.exception;

public class CustomExceptions {

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class InvalidLogImportException extends RuntimeException {
        public InvalidLogImportException(String message) {
            super(message);
        }
    }

    public static class CycleDetectedException extends RuntimeException {
        public CycleDetectedException(String message) {
            super(message);
        }
    }
}
