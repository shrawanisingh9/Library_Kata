package org.example.models;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class Book {

    @NotBlank(message = "BookId must not be blank")
    private final String bookId;
    @NotBlank(message = "Title cannot be blank")
    @Size(max = 200)
    private final String title;
    private final String author;
    @Positive(message = "Total copies must be greater than zero")
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