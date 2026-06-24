package org.example.controller;

import org.example.models.Book;
import org.example.models.BorrowRecord;
import org.example.models.User;
import org.example.service.LibraryService;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @PostMapping("/users")
    public ResponseEntity<String> addUser(@RequestBody User user) {
        libraryService.addUser(user);
        return ResponseEntity.ok("User added successfully");
    }

    @PostMapping("/books")
    public ResponseEntity<String> addBook(@RequestBody Book book) {
        libraryService.addBook(book);
        return ResponseEntity.ok("Book added successfully");
    }

    @PostMapping("/borrow")
    public ResponseEntity<String> borrowBook(@RequestParam String userId, @RequestParam String bookId) {
        libraryService.borrowBook(userId, bookId);
        return ResponseEntity.ok("Book borrowed successfully");
    }

    @PostMapping("/return")
    public ResponseEntity<String> returnBook(@RequestParam String userId, @RequestParam String bookId) {

        libraryService.returnBook(userId, bookId);
        return ResponseEntity.ok("Book returned successfully");
    }

    @GetMapping("/users/{userId}/books")
    public ResponseEntity<List<Book>> getBorrowedBooks(
            @PathVariable String userId) {

        return ResponseEntity.ok(
                libraryService.getBorrowedBooks(userId)
        );
    }

    @GetMapping("/users/{userId}/history")
    public List<BorrowRecord> getBorrowHistory(@PathVariable String userId) {
        return libraryService.getBorrowHistory(userId);
    }
}