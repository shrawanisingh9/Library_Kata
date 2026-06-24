package org.example.exceptions;

public class BookUnavailableException extends LibraryException {

    public BookUnavailableException(String bookId) {
        super("No copies available for book: " + bookId);
    }
}