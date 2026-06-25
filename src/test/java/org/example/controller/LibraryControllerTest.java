package org.example.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.exceptions.BookNotFoundException;
import org.example.models.Book;
import org.example.models.BorrowRecord;
import org.example.models.User;
import org.example.service.LibraryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LibraryController.class)
@Import(GlobalExceptionHandler.class)
class LibraryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private LibraryService libraryService;

    @Test
    @DisplayName("should add user")
    void shouldAddUser() throws Exception {
        User user = new User("U1", "John");

        mockMvc.perform(post("/api/library/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(content().string("User added successfully"));
    }

    @Test
    @DisplayName("should add book")
    void shouldAddBook() throws Exception {
        Book book = new Book("B1", "Clean Code",null,2);

        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk())
                .andExpect(content().string("Book added successfully"));
    }

    @Test
    @DisplayName("should borrow book")
    void shouldBorrowBook() throws Exception {
        mockMvc.perform(post("/api/library/borrow")
                        .param("userId", "U1")
                        .param("bookId", "B1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book borrowed successfully"));
    }

    @Test
    @DisplayName("should return book")
    void shouldReturnBook() throws Exception {
        mockMvc.perform(post("/api/library/return")
                        .param("userId", "U1")
                        .param("bookId", "B1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book returned successfully"));
    }

    @Test
    @DisplayName("should get borrowed books")
    void shouldGetBorrowedBooks() throws Exception {
        Book book = new Book("B1", "Clean Code", null,2);

        Mockito.when(libraryService.getBorrowedBooks("U1")).thenReturn(List.of(book));

        mockMvc.perform(get("/api/library/users/U1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("B1"));
    }

    @Test
    @DisplayName("should get borrow history")
    void shouldGetBorrowHistory() throws Exception {
        BorrowRecord record = new BorrowRecord("U1", "B1", LocalDateTime.now(), null);

        Mockito.when(libraryService.getBorrowHistory("U1")).thenReturn(List.of(record));

        mockMvc.perform(get("/api/library/users/U1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].userId").value("U1"));
    }

    @Test
    @DisplayName("should return 404 when service throws exception")
    void shouldReturn404() throws Exception {
        Mockito.when(libraryService.getBorrowedBooks("U1")).thenThrow(new BookNotFoundException("B1"));

        mockMvc.perform(get("/api/library/users/U1/books"))
                .andExpect(status().isNotFound());
    }

    //User Validation Tests
    @Test
    void shouldReturnBadRequestWhenUserIdIsBlank() throws Exception {
        User user = new User("", "John");

        mockMvc.perform(post("/api/library/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
        Mockito.verifyNoInteractions(libraryService);
    }

    //Book Validation Tests
    @Test
    void shouldReturnBadRequestWhenBookIdIsBlank() throws Exception {
        Book book = new Book("", "Clean Code", "Martin", 2);

        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTitleIsBlank() throws Exception {
        Book book = new Book("B1", "", "Robert Martin", 2);

        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTotalCopiesIsZero() throws Exception {
        Book book = new Book("B1", "Clean Code", "Martin", 0);

        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenTotalCopiesIsNegative() throws Exception {
        Book book = new Book("B1", "Clean Code", "Robert Martin", -1);

        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isBadRequest());
    }

    //RequestParam Validation Tests
    @Test
    void shouldReturnBadRequestWhenBorrowUserIdIsBlank() throws Exception {
        mockMvc.perform(post("/api/library/borrow")
                        .param("userId", "")
                        .param("bookId", "B1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenBorrowBookIdIsBlank() throws Exception {
        mockMvc.perform(post("/api/library/borrow")
                        .param("userId", "U1")
                        .param("bookId", ""))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenReturnUserIdIsBlank() throws Exception {
        mockMvc.perform(post("/api/library/return")
                        .param("userId", "")
                        .param("bookId", "B1"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturnBadRequestWhenReturnBookIdIsBlank() throws Exception {
        mockMvc.perform(post("/api/library/return")
                        .param("userId", "U1")
                        .param("bookId", ""))
                .andExpect(status().isBadRequest());
    }
}