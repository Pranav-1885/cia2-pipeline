package com.library.catalog.service;

import com.library.catalog.model.Book;

import java.util.List;

public interface BookService {

    List<Book> getAllBooks();

    Book getBookById(Long id);

    Book addBook(Book book);

    List<Book> searchBooks(String query);

    void deleteBook(Long id);

    void initializeSampleData();

    void resetCatalog();
}

