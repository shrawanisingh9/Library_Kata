package org.example.models;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Book {

    private final String bookId;
    private final String title;
    private final String author;
    private final int totalCopies;
    private int availableCopies;

    public Book(String bookId, String title, String author, int totalCopies) {
        this.bookId = bookId;
        this.title = title;
        this.author = author;
        this.totalCopies = totalCopies;
        this.availableCopies = totalCopies;
    }

    public synchronized void borrowCopy() {

        if (availableCopies <= 0)
            throw new IllegalStateException("No copies available");

        availableCopies--;
    }

    public synchronized void returnCopy() {

        if (availableCopies >= totalCopies)
            throw new IllegalStateException("Inventory corruption detected");

        availableCopies++;
    }
}