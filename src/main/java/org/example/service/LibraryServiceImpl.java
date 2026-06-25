package org.example.service;

import org.example.exceptions.*;
import org.example.models.Book;
import org.example.models.BorrowRecord;
import org.example.models.User;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.springframework.stereotype.Service;

@Service
public class LibraryServiceImpl implements LibraryService {

    private final Map<String, Book> books = new ConcurrentHashMap<>();
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final List<BorrowRecord> borrowRecords = new CopyOnWriteArrayList<>();
    private final Map<String, Set<String>> activeBooksByUser = new ConcurrentHashMap<>();
    private final Map<String, BorrowRecord> activeBorrowRecords = new ConcurrentHashMap<>();

    @Override
    public void addUser(User user) {
        users.put(user.getUserId(), user);
    }

    @Override
    public void addBook(Book book) {

        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (books.containsKey(book.getBookId())) {
            throw new DuplicateBookException(book.getBookId());
        }

        books.put(book.getBookId(), book);
    }

    @Override
    public void borrowBook(String userId, String bookId) {
        validateUser(userId);
        Book book = getBook(bookId);

        synchronized (book) {
            Set<String> borrowedBooks = activeBooksByUser.computeIfAbsent(userId, key -> ConcurrentHashMap.newKeySet());

            if (borrowedBooks.contains(bookId)) {
                throw new AlreadyBorrowedException(userId, bookId);
            }

            if (book.getAvailableCopies() <= 0) {
                throw new BookUnavailableException(bookId);
            }
            book.borrowCopy();

            BorrowRecord borrowRecord = new BorrowRecord(userId, bookId, LocalDateTime.now(), null);

            // history
            borrowRecords.add(borrowRecord);

            // active user books
            borrowedBooks.add(bookId);

            // active borrow transaction
            activeBorrowRecords.put(borrowKey(userId, bookId), borrowRecord);

        }
    }

    @Override
    public void returnBook(String userId, String bookId) {
        validateUser(userId);
        Book book = getBook(bookId);

        synchronized (book) {
            BorrowRecord activeBorrow = removeActiveBorrowRecord(userId, bookId);
            activeBorrow.setReturnedAt(LocalDateTime.now());
            Set<String> borrowedBooks = activeBooksByUser.get(userId);
            borrowedBooks.remove(bookId);

            if (borrowedBooks.isEmpty()) {
                activeBooksByUser.remove(userId);
            }

            book.returnCopy();
        }
    }

    @Override
    public List<Book> getBorrowedBooks(String userId) {
        validateUser(userId);
        Set<String> borrowedBooks = activeBooksByUser.getOrDefault(userId, Collections.emptySet());

        return borrowedBooks.stream()
                .map(this::getBook)
                .toList();
    }

    @Override
    public List<BorrowRecord> getBorrowHistory(String userId) {
        validateUser(userId);

        return borrowRecords.stream()
                .filter(record -> record.getUserId().equals(userId))
                .toList();
    }

    private void validateUser(String userId) {
        User user = users.get(userId);
        if (user == null) {
            throw new UserNotFoundException(userId);
        }
    }

    private Book getBook(String bookId) {
        Book book = books.get(bookId);
        if (book == null) {
            throw new BookNotFoundException(bookId);
        }
        return book;
    }

    private String borrowKey(String userId, String bookId) {
        return userId + ":" + bookId;
    }

    private BorrowRecord removeActiveBorrowRecord(String userId, String bookId) {
        BorrowRecord record = activeBorrowRecords.remove(borrowKey(userId, bookId));
        if (record == null) {
            throw new BorrowRecordNotFoundException(userId, bookId);
        }
        return record;
    }
}