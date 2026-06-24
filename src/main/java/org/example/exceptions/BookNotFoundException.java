package org.example.exceptions;

public class BookNotFoundException extends LibraryException {

    public BookNotFoundException(String bookId) {
        super("Book not found: " + bookId);
    }
}