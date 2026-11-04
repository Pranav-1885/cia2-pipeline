package com.library.catalog.service;

import com.library.catalog.exception.BookNotFoundException;
import com.library.catalog.exception.InvalidBookException;
import com.library.catalog.model.Book;
import com.library.catalog.repository.BookRepository;
import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;

import java.time.Year;
import java.util.List;

@Service
public class BookServiceImpl implements BookService {

    private final BookRepository bookRepository;

    public BookServiceImpl(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    @PostConstruct
    public void init() {
        initializeSampleData();
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Book getBookById(Long id) {
        if (id == null) {
            throw new InvalidBookException("Book ID cannot be null");
        }
        return bookRepository.findById(id)
                .orElseThrow(() -> new BookNotFoundException(id));
    }

    @Override
    public Book addBook(Book book) {
        validateBook(book);

        // Sanitize string inputs
        Book sanitizedBook = new Book();
        sanitizedBook.setTitle(book.getTitle().trim());
        sanitizedBook.setAuthor(book.getAuthor().trim());
        sanitizedBook.setGenre(
                (book.getGenre() == null || book.getGenre().trim().isEmpty())
                        ? "General"
                        : book.getGenre().trim()
        );
        sanitizedBook.setPublicationYear(book.getPublicationYear());

        return bookRepository.save(sanitizedBook);
    }

    @Override
    public List<Book> searchBooks(String query) {
        return bookRepository.search(query);
    }

    @Override
    public void deleteBook(Long id) {
        if (id == null) {
            throw new InvalidBookException("Book ID cannot be null");
        }
        if (!bookRepository.existsById(id)) {
            throw new BookNotFoundException(id);
        }
        bookRepository.deleteById(id);
    }

    @Override
    public synchronized void initializeSampleData() {
        if (bookRepository.count() == 0) {
            bookRepository.save(new Book(null, "The Great Gatsby", "F. Scott Fitzgerald", "Classic Fiction", 1925));
            bookRepository.save(new Book(null, "To Kill a Mockingbird", "Harper Lee", "Classic Fiction", 1960));
            bookRepository.save(new Book(null, "1984", "George Orwell", "Dystopian", 1949));
            bookRepository.save(new Book(null, "Clean Code", "Robert C. Martin", "Technology", 2008));
            bookRepository.save(new Book(null, "The Hobbit", "J.R.R. Tolkien", "Fantasy", 1937));
        }
    }

    @Override
    public synchronized void resetCatalog() {
        bookRepository.deleteAll();
    }

    private void validateBook(Book book) {
        if (book == null) {
            throw new InvalidBookException("Book payload cannot be null");
        }

        if (book.getTitle() == null || book.getTitle().trim().isEmpty()) {
            throw new InvalidBookException("Book title cannot be blank");
        }

        if (book.getAuthor() == null || book.getAuthor().trim().isEmpty()) {
            throw new InvalidBookException("Book author cannot be blank");
        }

        if (book.getPublicationYear() == null) {
            throw new InvalidBookException("Publication year cannot be null");
        }

        int currentYear = Year.now().getValue();
        if (book.getPublicationYear() <= 0) {
            throw new InvalidBookException("Publication year must be greater than 0");
        }

        if (book.getPublicationYear() > currentYear + 10) {
            throw new InvalidBookException("Publication year cannot be in the distant future (max " + (currentYear + 10) + ")");
        }
    }
}

