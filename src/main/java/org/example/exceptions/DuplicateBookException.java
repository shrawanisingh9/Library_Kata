package org.example.exceptions;

public class DuplicateBookException extends LibraryException {

    public DuplicateBookException(String bookId) {
        super("Book already exists: " + bookId);
    }
}