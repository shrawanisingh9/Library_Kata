package org.example.component;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.models.Book;
import org.example.models.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class LibraryComponentTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void shouldBorrowAndReturnBookSuccessfully() throws Exception {
        User user = new User("U1", "John");
        Book book = new Book("B1", "Clean Code", null, 2);

        // Add user
        mockMvc.perform(post("/api/library/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        // Add book
        mockMvc.perform(post("/api/library/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(book)))
                .andExpect(status().isOk());

        // Borrow
        mockMvc.perform(post("/api/library/borrow")
                        .param("userId", "U1")
                        .param("bookId", "B1"))
                .andExpect(status().isOk())
                .andExpect(content().string("Book borrowed successfully"));

        // Verify borrowed books
        mockMvc.perform(get("/api/library/users/U1/books"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("B1"));

        // Return
        mockMvc.perform(post("/api/library/return")
                        .param("userId", "U1")
                        .param("bookId", "B1"))
                .andExpect(status().isOk());

        // Verify empty
        mockMvc.perform(get("/api/library/users/U1/books"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));

        // Verify borrow history
        mockMvc.perform(get("/api/library/users/U1/history"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].bookId").value("B1"));

    }

    @Test
    void shouldReturn404WhenBookNotFound() throws Exception {
        User user = new User("U1", "John");

        mockMvc.perform(post("/api/library/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/library/borrow")
                        .param("userId", "U1")
                        .param("bookId", "UNKNOWN"))
                .andExpect(status().isNotFound());
    }
}