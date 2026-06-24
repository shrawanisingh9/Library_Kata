package org.example.service;

import org.example.models.Book;
import org.example.models.BorrowRecord;
import org.example.models.User;

import java.util.List;

public interface LibraryService {
    void addUser(User user);
    void addBook(Book book);
    void borrowBook(String userId, String bookId);
    void returnBook(String userId, String bookId);
    List<Book> getBorrowedBooks(String userId);
    List<BorrowRecord> getBorrowHistory(String userId);
}