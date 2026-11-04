package com.library.catalog.controller;

import com.library.catalog.model.Book;
import com.library.catalog.service.BookService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class BookWebControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private BookService bookService;

    @BeforeEach
    void setUp() {
        bookService.resetCatalog();
        bookService.initializeSampleData();
    }

    @Test
    @DisplayName("GET / - Should redirect to /books")
    void testRootRedirectsToBooks() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"));
    }

    @Test
    @DisplayName("GET /books - Should render catalog page with books, title, and version v1.0")
    void testListBooksView() throws Exception {
        mockMvc.perform(get("/books"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attributeExists("books"))
                .andExpect(model().attribute("appVersion", "v1.0"))
                .andExpect(model().attribute("appName", "Library Book Catalog"))
                .andExpect(content().string(containsString("Library Book Catalog")))
                .andExpect(content().string(containsString("v1.0")))
                .andExpect(content().string(containsString("The Great Gatsby")))
                .andExpect(content().string(containsString("To Kill a Mockingbird")));
    }

    @Test
    @DisplayName("GET /books?query=Hobbit - Should filter search results")
    void testSearchBooksInView() throws Exception {
        mockMvc.perform(get("/books").param("query", "Hobbit"))
                .andExpect(status().isOk())
                .andExpect(view().name("books"))
                .andExpect(model().attribute("isSearching", true))
                .andExpect(content().string(containsString("The Hobbit")))
                .andExpect(content().string(not(containsString("Clean Code"))));
    }

    @Test
    @DisplayName("GET /books?query=Nonexistent - Should show empty search state")
    void testSearchNoResultsEmptyState() throws Exception {
        mockMvc.perform(get("/books").param("query", "NonExistentBookXYZ123"))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("No matching books found")))
                .andExpect(content().string(containsString("empty-search-state")));
    }

    @Test
    @DisplayName("POST /books - Should add new book and redirect to /books with success flash message")
    void testAddBookViaForm() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "The Pragmatic Programmer")
                        .param("author", "David Thomas")
                        .param("genre", "Software")
                        .param("publicationYear", "1999"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("POST /books - Should reject invalid book and redirect with error message")
    void testAddInvalidBookViaForm() throws Exception {
        mockMvc.perform(post("/books")
                        .param("title", "")
                        .param("author", "Unknown")
                        .param("genre", "Unknown")
                        .param("publicationYear", "2020"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("POST /books/delete/{id} - Should delete book and redirect to /books")
    void testDeleteBookViaForm() throws Exception {
        Book book = bookService.addBook(new Book(null, "Book To Delete", "Author", "Genre", 2020));

        mockMvc.perform(post("/books/delete/{id}", book.getId()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/books"))
                .andExpect(flash().attributeExists("successMessage"));
    }
}

