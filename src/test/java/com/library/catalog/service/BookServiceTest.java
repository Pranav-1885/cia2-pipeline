package com.library.catalog.service;

import com.library.catalog.exception.BookNotFoundException;
import com.library.catalog.exception.InvalidBookException;
import com.library.catalog.model.Book;
import com.library.catalog.repository.InMemoryBookRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Year;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookServiceTest {

    private BookServiceImpl bookService;
    private InMemoryBookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
        bookService = new BookServiceImpl(repository);
    }

    @Test
    @DisplayName("Should initialize and retrieve default catalog sample books")
    void testInitializeAndRetrieveCatalog() {
        bookService.initializeSampleData();

        List<Book> books = bookService.getAllBooks();

        assertThat(books).isNotEmpty();
        assertThat(books.size()).isGreaterThanOrEqualTo(3);
    }

    @Test
    @DisplayName("Should successfully add a new book")
    void testAddBookSuccess() {
        Book book = new Book(null, "Refactoring", "Martin Fowler", "Software Design", 1999);

        Book added = bookService.addBook(book);

        assertThat(added.getId()).isNotNull().isPositive();
        assertThat(added.getTitle()).isEqualTo("Refactoring");
        assertThat(added.getAuthor()).isEqualTo("Martin Fowler");
        assertThat(added.getGenre()).isEqualTo("Software Design");
        assertThat(added.getPublicationYear()).isEqualTo(1999);

        Book fetched = bookService.getBookById(added.getId());
        assertThat(fetched).isNotNull();
        assertThat(fetched.getTitle()).isEqualTo("Refactoring");
    }

    @Test
    @DisplayName("Should generate unique IDs for sequentially added books")
    void testUniqueIdGeneration() {
        Book b1 = bookService.addBook(new Book(null, "Title 1", "Author 1", "Genre", 2000));
        Book b2 = bookService.addBook(new Book(null, "Title 2", "Author 2", "Genre", 2001));
        Book b3 = bookService.addBook(new Book(null, "Title 3", "Author 3", "Genre", 2002));

        assertThat(b1.getId()).isNotEqualTo(b2.getId());
        assertThat(b2.getId()).isNotEqualTo(b3.getId());
        assertThat(b1.getId()).isNotEqualTo(b3.getId());
    }

    @Test
    @DisplayName("Should search books by title")
    void testSearchByTitle() {
        bookService.addBook(new Book(null, "The Pragmatic Programmer", "Andy Hunt", "Software", 1999));
        bookService.addBook(new Book(null, "Clean Architecture", "Robert C. Martin", "Software", 2017));

        List<Book> results = bookService.searchBooks("pragmatic");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("The Pragmatic Programmer");
    }

    @Test
    @DisplayName("Should search books by author")
    void testSearchByAuthor() {
        bookService.addBook(new Book(null, "Design Patterns", "Erich Gamma", "Software", 1994));
        bookService.addBook(new Book(null, "Test-Driven Development", "Kent Beck", "Software", 2002));

        List<Book> results = bookService.searchBooks("Gamma");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor()).isEqualTo("Erich Gamma");
    }

    @Test
    @DisplayName("Should delete an existing book")
    void testDeleteExistingBook() {
        Book book = bookService.addBook(new Book(null, "To Be Deleted", "Someone", "Misc", 2020));
        Long id = book.getId();

        bookService.deleteBook(id);

        assertThatThrownBy(() -> bookService.getBookById(id))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: " + id);
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when getting nonexistent ID")
    void testGetNonexistentBook() {
        assertThatThrownBy(() -> bookService.getBookById(9999L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: 9999");
    }

    @Test
    @DisplayName("Should throw BookNotFoundException when deleting nonexistent ID")
    void testDeleteNonexistentBook() {
        assertThatThrownBy(() -> bookService.deleteBook(9999L))
                .isInstanceOf(BookNotFoundException.class)
                .hasMessageContaining("Book not found with id: 9999");
    }

    @Test
    @DisplayName("Should reject null book payload")
    void testRejectNullBook() {
        assertThatThrownBy(() -> bookService.addBook(null))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("Book payload cannot be null");
    }

    @Test
    @DisplayName("Should reject blank or empty title")
    void testRejectBlankTitle() {
        Book book = new Book(null, "   ", "Valid Author", "Fiction", 2020);
        assertThatThrownBy(() -> bookService.addBook(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("Book title cannot be blank");
    }

    @Test
    @DisplayName("Should reject blank or empty author")
    void testRejectBlankAuthor() {
        Book book = new Book(null, "Valid Title", "  ", "Fiction", 2020);
        assertThatThrownBy(() -> bookService.addBook(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("Book author cannot be blank");
    }

    @Test
    @DisplayName("Should reject null publication year")
    void testRejectNullPublicationYear() {
        Book book = new Book(null, "Valid Title", "Valid Author", "Fiction", null);
        assertThatThrownBy(() -> bookService.addBook(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("Publication year cannot be null");
    }

    @Test
    @DisplayName("Should reject zero or negative publication year")
    void testRejectNonPositivePublicationYear() {
        Book book = new Book(null, "Valid Title", "Valid Author", "Fiction", 0);
        assertThatThrownBy(() -> bookService.addBook(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("Publication year must be greater than 0");

        Book bookNegative = new Book(null, "Valid Title", "Valid Author", "Fiction", -50);
        assertThatThrownBy(() -> bookService.addBook(bookNegative))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("Publication year must be greater than 0");
    }

    @Test
    @DisplayName("Should reject distant future publication year")
    void testRejectFuturePublicationYear() {
        int futureYear = Year.now().getValue() + 50;
        Book book = new Book(null, "Valid Title", "Valid Author", "Fiction", futureYear);
        assertThatThrownBy(() -> bookService.addBook(book))
                .isInstanceOf(InvalidBookException.class)
                .hasMessageContaining("distant future");
    }

    @Test
    @DisplayName("Should default blank genre to General")
    void testDefaultBlankGenre() {
        Book book = new Book(null, "Generic Title", "Generic Author", "   ", 2021);
        Book saved = bookService.addBook(book);
        assertThat(saved.getGenre()).isEqualTo("General");
    }
}

