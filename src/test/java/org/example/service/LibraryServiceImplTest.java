package org.example.service;

import org.example.exceptions.*;
import org.example.models.Book;
import org.example.models.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

class LibraryServiceImplTest {

    private static final String USER_ID = "U1";
    private static final String BOOK_ID = "B1";

    private LibraryServiceImpl service;
    private Book book;
    private User user;

    @BeforeEach
    void setup() {
        service = new LibraryServiceImpl();
        user = new User(USER_ID, "John");
        book = new Book(BOOK_ID, "Clean Code", "Robert Martin", 2);
        service.addUser(user);
        service.addBook(book);
    }

    @Nested
    class AddBookTests {

        @Test
        void shouldAddBookSuccessfully() {
            assertDoesNotThrow(() -> service.addBook(new Book("B2", "Cleaner Code", "Bob", 2)));
        }

        @Test
        void shouldThrowWhenBookAlreadyExists() {
            assertThrows(DuplicateBookException.class, () -> service.addBook(book));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("org.example.service.LibraryServiceImplTest#addBookNegativeCases")
        void shouldThrowWhenAddingInvalidBook(String scenario, Book invalidBook) {
            assertThrows(IllegalArgumentException.class, () -> service.addBook(invalidBook));
        }
    }

    @Nested
    class BorrowBookTests {

        @Test
        void shouldBorrowBookSuccessfully() {
            service.borrowBook(USER_ID, BOOK_ID);
            List<Book> books = service.getBorrowedBooks(USER_ID);

            assertAll(
                    () -> assertEquals(1, books.size()),
                    () -> assertEquals(BOOK_ID, books.getFirst().getBookId())
            );
        }

        @Test
        void shouldThrowWhenBorrowingSameBookTwice() {
            service.borrowBook(USER_ID, BOOK_ID);
            assertThrows(AlreadyBorrowedException.class, () -> service.borrowBook(USER_ID, BOOK_ID));
        }

        @Test
        void shouldThrowWhenBookUnavailable() {
            Book singleCopyBook = new Book("B2", "DDD", "Evans", 1);
            User user2 = new User("U2", "Jane");
            service.addBook(singleCopyBook);
            service.addUser(user2);
            service.borrowBook(USER_ID, "B2");

            assertThrows(BookUnavailableException.class, () -> service.borrowBook("U2", "B2"));
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("borrowNegativeCases")
        void shouldThrowExpectedExceptionWhenBorrowing(String scenario, String userId, String bookId, Class<? extends Throwable> exceptionType) {
            assertThrows(exceptionType, () -> service.borrowBook(userId, bookId));
        }

        private static Stream<Arguments> borrowNegativeCases() {
            return Stream.of(
                    Arguments.of("Unknown user", "UNKNOWN", BOOK_ID, UserNotFoundException.class),
                    Arguments.of("Unknown book", USER_ID, "INVALID", BookNotFoundException.class)
            );
        }
    }

    @Nested
    class ReturnBookTests {

        @Test
        void shouldThrowWhenReturningBookNotBorrowed() {
            assertThrows(BorrowRecordNotFoundException.class, () -> service.returnBook(USER_ID, BOOK_ID));
        }

        @Test
        void shouldReturnBookSuccessfully() {
            service.borrowBook(USER_ID, BOOK_ID);
            service.returnBook(USER_ID, BOOK_ID);

            assertTrue(service.getBorrowedBooks(USER_ID).isEmpty());
        }

        @Test
        void shouldThrowWhenReturningBookTwice() {
            service.borrowBook(USER_ID, BOOK_ID);
            service.returnBook(USER_ID, BOOK_ID);

            assertThrows(BorrowRecordNotFoundException.class, () -> service.returnBook(USER_ID, BOOK_ID));
        }

        @Test
        void shouldKeepOtherBorrowedBooksWhenReturningOneBook() {
            Book secondBook = new Book("B2", "DDD", "Eric Evans", 1);
            service.addBook(secondBook);
            service.borrowBook(USER_ID, BOOK_ID);
            service.borrowBook(USER_ID, "B2");
            service.returnBook(USER_ID, BOOK_ID);

            List<Book> borrowedBooks = service.getBorrowedBooks(USER_ID);

            assertAll(
                    () -> assertEquals(1, borrowedBooks.size()),
                    () -> assertEquals("B2", borrowedBooks.getFirst().getBookId())
            );
        }
    }

    @Nested
    class GetBorrowedBooksTests {

        @Test
        void shouldReturnEmptyListWhenNothingBorrowed() {
            List<Book> books = service.getBorrowedBooks(USER_ID);
            assertTrue(books.isEmpty());
        }

        @Test
        void shouldThrowWhenGettingBooksForUnknownUser() {
            assertThrows(UserNotFoundException.class, () -> service.getBorrowedBooks("UNKNOWN"));
        }

        @Test
        void shouldReturnAllBorrowedBooks() {
            Book b2 = new Book("B2", "DDD", "Evans", 1);
            Book b3 = new Book("B3", "Refactoring", "Martin Fowler", 1);

            service.addBook(b2);
            service.addBook(b3);

            service.borrowBook(USER_ID, "B2");
            service.borrowBook(USER_ID, "B3");

            List<Book> books = service.getBorrowedBooks(USER_ID);

            assertEquals(2, books.size());
        }

        @ParameterizedTest(name = "{0}")
        @MethodSource("invalidInputCases")
        void shouldThrowWhenInputIsInvalid(String scenario, String operation, String userId, String bookId) {
            assertThrows(IllegalArgumentException.class,
                    () -> {
                        switch (operation) {
                            case "borrow" -> service.borrowBook(userId, bookId);
                            case "return" -> service.returnBook(userId, bookId);
                            case "getBorrowedBooks" -> service.getBorrowedBooks(userId);
                            default -> throw new IllegalArgumentException(
                                    "Unknown operation: " + operation);
                        }});
        }

        private static Stream<Arguments> invalidInputCases() {
            return Stream.of(
                    Arguments.of("Borrow with null user id", "borrow", null, BOOK_ID),
                    Arguments.of("Borrow with blank user id", "borrow", "", BOOK_ID),
                    Arguments.of("Borrow with null book id", "borrow", USER_ID, null),
                    Arguments.of("Return with blank user id", "return", "", BOOK_ID),
                    Arguments.of("Get borrowed books with blank user id", "getBorrowedBooks", "", null)
            );
        }
    }

    private static Stream<Arguments> addBookNegativeCases() {
        return Stream.of(
                Arguments.of("Book is null", null),
                Arguments.of("Book ID is blank", new Book("", "Clean Code", "Bob", 1)),
                Arguments.of("Title is blank", new Book("B2", "", "Bob", 1)),
                Arguments.of("Title is null", new Book("B2", null, "Bob", 1)),
                Arguments.of("Total copies is negative", new Book("B2", "Clean Code", "Bob", -1))
        );
    }
}