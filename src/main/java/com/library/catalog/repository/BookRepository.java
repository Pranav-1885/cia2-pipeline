package com.library.catalog.repository;

import com.library.catalog.model.Book;

import java.util.List;
import java.util.Optional;

public interface BookRepository {

    List<Book> findAll();

    Optional<Book> findById(Long id);

    Book save(Book book);

    boolean deleteById(Long id);

    boolean existsById(Long id);

    List<Book> search(String query);

    long count();

    void deleteAll();
}

