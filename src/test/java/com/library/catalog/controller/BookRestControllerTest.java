package com.library.catalog.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.library.catalog.model.Book;
import com.library.catalog.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookService bookService;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        bookService.resetCatalog();
        bookService.initializeSampleData();
    }

    @Test
    @DisplayName("GET /api/books - Should retrieve all books")
    void testGetAllBooks() throws Exception {
        mockMvc.perform(get("/api/books")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(greaterThanOrEqualTo(5))))
                .andExpect(jsonPath("$[0].title", notNullValue()))
                .andExpect(jsonPath("$[0].author", notNullValue()));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should retrieve book by ID when present")
    void testGetBookByIdSuccess() throws Exception {
        Book book = bookService.addBook(new Book(null, "Test Driven Development", "Kent Beck", "Software", 2002));

        mockMvc.perform(get("/api/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(book.getId().intValue())))
                .andExpect(jsonPath("$.title", is("Test Driven Development")))
                .andExpect(jsonPath("$.author", is("Kent Beck")));
    }

    @Test
    @DisplayName("GET /api/books/{id} - Should return 404 when book ID does not exist")
    void testGetBookByIdNotFound() throws Exception {
        mockMvc.perform(get("/api/books/{id}", 99999L)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Book not found with id: 99999")));
    }

    @Test
    @DisplayName("POST /api/books - Should create new book and return 201 Created")
    void testCreateBookSuccess() throws Exception {
        Book newBook = new Book(null, "Clean Architecture", "Robert C. Martin", "Software Design", 2017);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newBook)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.title", is("Clean Architecture")))
                .andExpect(jsonPath("$.author", is("Robert C. Martin")))
                .andExpect(jsonPath("$.genre", is("Software Design")))
                .andExpect(jsonPath("$.publicationYear", is(2017)));
    }

    @Test
    @DisplayName("POST /api/books - Should return 400 Bad Request when title is blank")
    void testCreateBookBlankTitle() throws Exception {
        Book invalidBook = new Book(null, "", "Robert C. Martin", "Software", 2017);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/books - Should return 400 Bad Request when author is blank")
    void testCreateBookBlankAuthor() throws Exception {
        Book invalidBook = new Book(null, "Clean Architecture", "   ", "Software", 2017);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)))
                .andExpect(jsonPath("$.message", notNullValue()));
    }

    @Test
    @DisplayName("POST /api/books - Should return 400 Bad Request when publication year is negative")
    void testCreateBookInvalidYear() throws Exception {
        Book invalidBook = new Book(null, "Clean Architecture", "Robert C. Martin", "Software", -10);

        mockMvc.perform(post("/api/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidBook)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is(400)));
    }

    @Test
    @DisplayName("GET /api/books/search - Should search books by title or author")
    void testSearchBooks() throws Exception {
        mockMvc.perform(get("/api/books/search")
                        .param("query", "Orwell")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title", is("1984")))
                .andExpect(jsonPath("$[0].author", is("George Orwell")));
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should delete existing book and return 204 No Content")
    void testDeleteBookSuccess() throws Exception {
        Book book = bookService.addBook(new Book(null, "Ephemeral Book", "Temporary Author", "None", 2020));

        mockMvc.perform(delete("/api/books/{id}", book.getId()))
                .andExpect(status().isNoContent());

        // Verify book is no longer present
        mockMvc.perform(get("/api/books/{id}", book.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/books/{id} - Should return 404 when deleting nonexistent book")
    void testDeleteBookNotFound() throws Exception {
        mockMvc.perform(delete("/api/books/{id}", 88888L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status", is(404)))
                .andExpect(jsonPath("$.message", containsString("Book not found with id: 88888")));
    }
}

