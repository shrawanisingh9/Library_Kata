package org.example.exceptions;

public class BorrowRecordNotFoundException extends LibraryException {

    public BorrowRecordNotFoundException(
            String userId,
            String bookId) {

        super(
            String.format(
                "No active borrow record found for user %s and book %s",
                userId,
                bookId
            )
        );
    }
}