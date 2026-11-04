package com.library.catalog.repository;

import com.library.catalog.model.Book;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class BookRepositoryTest {

    private InMemoryBookRepository repository;

    @BeforeEach
    void setUp() {
        repository = new InMemoryBookRepository();
    }

    @Test
    @DisplayName("Should save a book and generate a unique positive ID")
    void testSaveGeneratesUniqueId() {
        Book book1 = new Book(null, "Test Book 1", "Author 1", "Fiction", 2020);
        Book book2 = new Book(null, "Test Book 2", "Author 2", "Non-Fiction", 2021);

        Book saved1 = repository.save(book1);
        Book saved2 = repository.save(book2);

        assertThat(saved1.getId()).isNotNull().isPositive();
        assertThat(saved2.getId()).isNotNull().isPositive();
        assertThat(saved1.getId()).isNotEqualTo(saved2.getId());
    }

    @Test
    @DisplayName("Should find book by ID")
    void testFindById() {
        Book saved = repository.save(new Book(null, "Findable Book", "Known Author", "Sci-Fi", 2018));

        Optional<Book> found = repository.findById(saved.getId());

        assertThat(found).isPresent();
        assertThat(found.get().getTitle()).isEqualTo("Findable Book");
        assertThat(found.get().getAuthor()).isEqualTo("Known Author");
    }

    @Test
    @DisplayName("Should return empty optional for nonexistent ID")
    void testFindByIdNonexistent() {
        Optional<Book> found = repository.findById(99999L);
        assertThat(found).isEmpty();
    }

    @Test
    @DisplayName("Should find all saved books")
    void testFindAll() {
        repository.save(new Book(null, "Book A", "Author A", "Genre A", 2000));
        repository.save(new Book(null, "Book B", "Author B", "Genre B", 2005));

        List<Book> all = repository.findAll();

        assertThat(all).hasSize(2);
    }

    @Test
    @DisplayName("Should delete book by ID")
    void testDeleteById() {
        Book saved = repository.save(new Book(null, "To Delete", "Author", "Genre", 2010));
        assertThat(repository.existsById(saved.getId())).isTrue();

        boolean deleted = repository.deleteById(saved.getId());

        assertThat(deleted).isTrue();
        assertThat(repository.existsById(saved.getId())).isFalse();
        assertThat(repository.findById(saved.getId())).isEmpty();
    }

    @Test
    @DisplayName("Should search books by title case-insensitively")
    void testSearchByTitle() {
        repository.save(new Book(null, "The Great Gatsby", "F. Scott Fitzgerald", "Classic", 1925));
        repository.save(new Book(null, "1984", "George Orwell", "Dystopian", 1949));

        List<Book> results = repository.search("gatsby");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getTitle()).isEqualTo("The Great Gatsby");
    }

    @Test
    @DisplayName("Should search books by author case-insensitively")
    void testSearchByAuthor() {
        repository.save(new Book(null, "Animal Farm", "George Orwell", "Satire", 1945));
        repository.save(new Book(null, "Clean Code", "Robert C. Martin", "Tech", 2008));

        List<Book> results = repository.search("orwell");

        assertThat(results).hasSize(1);
        assertThat(results.get(0).getAuthor()).isEqualTo("George Orwell");
    }

    @Test
    @DisplayName("Should clear all books and reset ID counter")
    void testDeleteAll() {
        repository.save(new Book(null, "Book 1", "Author", "Genre", 2000));
        repository.deleteAll();

        assertThat(repository.count()).isZero();
        assertThat(repository.findAll()).isEmpty();

        Book nextBook = repository.save(new Book(null, "Book Fresh", "Author Fresh", "Genre", 2022));
        assertThat(nextBook.getId()).isEqualTo(1L);
    }
}

