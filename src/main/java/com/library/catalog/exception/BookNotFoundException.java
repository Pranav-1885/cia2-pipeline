package com.library.catalog.exception;

public class BookNotFoundException extends RuntimeException {
    public BookNotFoundException(Long id) {
        super("Book not found with id: " + id);
    }

    public BookNotFoundException(String message) {
        super(message);
    }
}

