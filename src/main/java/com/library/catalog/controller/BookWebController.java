package com.library.catalog.controller;

import com.library.catalog.exception.InvalidBookException;
import com.library.catalog.model.Book;
import com.library.catalog.service.BookService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class BookWebController {

    private final BookService bookService;

    @Value("${app.name:Library Book Catalog}")
    private String appName;

    @Value("${app.description:A simple, responsive web application for managing library book collections.}")
    private String appDescription;

    @Value("${app.version:v1.0}")
    private String appVersion;

    public BookWebController(BookService bookService) {
        this.bookService = bookService;
    }

    @GetMapping("/")
    public String index() {
        return "redirect:/books";
    }

    @GetMapping("/books")
    public String listBooks(@RequestParam(name = "query", required = false) String query,
                            Model model) {
        List<Book> books;
        boolean isSearching = query != null && !query.trim().isEmpty();

        if (isSearching) {
            books = bookService.searchBooks(query);
        } else {
            books = bookService.getAllBooks();
        }

        model.addAttribute("appName", appName);
        model.addAttribute("appDescription", appDescription);
        model.addAttribute("appVersion", appVersion);
        model.addAttribute("books", books);
        model.addAttribute("query", query != null ? query.trim() : "");
        model.addAttribute("isSearching", isSearching);

        if (!model.containsAttribute("newBook")) {
            model.addAttribute("newBook", new Book());
        }

        return "books";
    }

    @PostMapping("/books")
    public String addBook(@ModelAttribute("newBook") Book book,
                          RedirectAttributes redirectAttributes) {
        try {
            Book created = bookService.addBook(book);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Book \"" + created.getTitle() + "\" was successfully added to the catalog!");
        } catch (InvalidBookException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
            redirectAttributes.addFlashAttribute("newBook", book);
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to add book: " + ex.getMessage());
            redirectAttributes.addFlashAttribute("newBook", book);
        }
        return "redirect:/books";
    }

    @PostMapping("/books/delete/{id}")
    public String deleteBook(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookService.deleteBook(id);
            redirectAttributes.addFlashAttribute("successMessage", "Book (ID: " + id + ") was deleted successfully.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to delete book: " + ex.getMessage());
        }
        return "redirect:/books";
    }
}

