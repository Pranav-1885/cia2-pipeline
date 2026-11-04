package com.library.catalog.repository;

import com.library.catalog.model.Book;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Repository
public class InMemoryBookRepository implements BookRepository {

    private final Map<Long, Book> storage = new ConcurrentHashMap<>();
    private final AtomicLong idGenerator = new AtomicLong(0);

    @Override
    public List<Book> findAll() {
        return storage.values().stream()
                .sorted(Comparator.comparing(Book::getId))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public Optional<Book> findById(Long id) {
        if (id == null) {
            return Optional.empty();
        }
        Book book = storage.get(id);
        return Optional.ofNullable(copy(book));
    }

    @Override
    public synchronized Book save(Book book) {
        if (book == null) {
            throw new IllegalArgumentException("Book cannot be null");
        }

        if (book.getId() == null || book.getId() <= 0) {
            book.setId(idGenerator.incrementAndGet());
        } else {
            // Keep generator ahead of any manually set IDs
            idGenerator.updateAndGet(current -> Math.max(current, book.getId()));
        }

        Book savedCopy = copy(book);
        storage.put(savedCopy.getId(), savedCopy);
        return copy(savedCopy);
    }

    @Override
    public boolean deleteById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.remove(id) != null;
    }

    @Override
    public boolean existsById(Long id) {
        if (id == null) {
            return false;
        }
        return storage.containsKey(id);
    }

    @Override
    public List<Book> search(String query) {
        if (query == null || query.trim().isEmpty()) {
            return findAll();
        }

        String normalizedQuery = query.trim().toLowerCase();
        return storage.values().stream()
                .filter(b -> (b.getTitle() != null && b.getTitle().toLowerCase().contains(normalizedQuery)) ||
                             (b.getAuthor() != null && b.getAuthor().toLowerCase().contains(normalizedQuery)))
                .sorted(Comparator.comparing(Book::getId))
                .map(this::copy)
                .collect(Collectors.toList());
    }

    @Override
    public long count() {
        return storage.size();
    }

    @Override
    public synchronized void deleteAll() {
        storage.clear();
        idGenerator.set(0);
    }

    private Book copy(Book original) {
        if (original == null) {
            return null;
        }
        return new Book(
                original.getId(),
                original.getTitle(),
                original.getAuthor(),
                original.getGenre(),
                original.getPublicationYear()
        );
    }
}

