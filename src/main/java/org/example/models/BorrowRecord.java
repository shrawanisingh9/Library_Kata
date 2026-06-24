package org.example.models;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@AllArgsConstructor
public class BorrowRecord {
    private final String userId;
    private final String bookId;
    private final LocalDateTime borrowedAt;
    private LocalDateTime returnedAt;

    public boolean isActive() {
        return returnedAt == null;
    }
}