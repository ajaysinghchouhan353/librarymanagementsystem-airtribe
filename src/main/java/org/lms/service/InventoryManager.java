package org.lms.service;

import org.lms.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Manages book inventory operations.
 * Single Responsibility: Handle book catalog and availability.
 */
public class InventoryManager {
    private static final Logger log = LoggerFactory.getLogger(InventoryManager.class);
    
    private final Map<String, Book> inventory = new HashMap<>(); // isbn -> Book

    /**
     * Add a book to the inventory.
     */
    public synchronized void addBook(Book book) {
        inventory.put(book.getIsbn(), book);
        log.info("Book added: {}", book);
    }

    /**
     * Remove a book from the inventory.
     */
    public synchronized void removeBook(String isbn) {
        inventory.remove(isbn);
        log.info("Book removed: {}", isbn);
    }

    /**
     * Update an existing book in the inventory.
     */
    public synchronized void updateBook(Book book) {
        inventory.put(book.getIsbn(), book);
        log.info("Book updated: {}", book);
    }

    /**
     * Find a book by ISBN.
     */
    public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(inventory.get(isbn));
    }

    /**
     * Find books by title (case-insensitive partial match).
     */
    public List<Book> findByTitle(String title) {
        String lowerTitle = title.toLowerCase();
        return inventory.values().stream()
                .filter(b -> b.getTitle().toLowerCase().contains(lowerTitle))
                .collect(Collectors.toList());
    }

    /**
     * Find books by author (case-insensitive partial match).
     */
    public List<Book> findByAuthor(String author) {
        String lowerAuthor = author.toLowerCase();
        return inventory.values().stream()
                .filter(b -> b.getAuthor().toLowerCase().contains(lowerAuthor))
                .collect(Collectors.toList());
    }

    /**
     * Check if a book exists in the inventory.
     */
    public boolean hasBook(String isbn) {
        return inventory.containsKey(isbn);
    }

    /**
     * Get all books in the inventory.
     */
    public List<Book> getAllBooks() {
        return new ArrayList<>(inventory.values());
    }

    /**
     * Get the total number of books in the inventory.
     */
    public int getTotalBooks() {
        return inventory.size();
    }
}
