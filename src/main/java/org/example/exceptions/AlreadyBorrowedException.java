package org.example.exceptions;

public class AlreadyBorrowedException extends LibraryException {

    public AlreadyBorrowedException(String userId, String bookId) {
        super(
            String.format(
                "User %s has already borrowed book %s",
                userId,
                bookId
            )
        );
    }
}