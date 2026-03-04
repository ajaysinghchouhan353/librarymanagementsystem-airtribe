package org.lms.service;

import org.lms.entity.Patron;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages patron operations.
 * Single Responsibility: Handle patron registration and information.
 */
public class PatronManager {
    private static final Logger log = LoggerFactory.getLogger(PatronManager.class);
    
    private final Map<String, Patron> patrons = new HashMap<>(); // patronId -> Patron

    /**
     * Add a new patron.
     */
    public synchronized void addPatron(Patron patron) {
        patrons.put(patron.getId(), patron);
        log.info("Patron added: {}", patron);
    }

    /**
     * Remove a patron from the system.
     */
    public synchronized void removePatron(String patronId) {
        Patron removed = patrons.remove(patronId);
        if (removed != null) {
            log.info("Patron removed: {}", removed);
        }
    }

    /**
     * Update an existing patron's information.
     */
    public synchronized void updatePatron(Patron patron) {
        patrons.put(patron.getId(), patron);
        log.info("Patron updated: {}", patron);
    }

    /**
     * Find a patron by ID.
     */
    public Optional<Patron> findPatronById(String id) {
        return Optional.ofNullable(patrons.get(id));
    }

    /**
     * Check if a patron exists.
     */
    public boolean hasPatron(String patronId) {
        return patrons.containsKey(patronId);
    }

    /**
     * Get all patrons.
     */
    public List<Patron> getAllPatrons() {
        return new ArrayList<>(patrons.values());
    }

    /**
     * Record that a patron borrowed a book.
     */
    public synchronized void recordBorrow(String patronId, String isbn) {
        findPatronById(patronId).ifPresent(patron -> {
            patron.recordBorrowedBook(isbn);
            log.debug("Recorded borrow: PatronID={}, ISBN={}", patronId, isbn);
        });
    }

    /**
     * Get a patron's borrowing history.
     */
    public List<String> getPatronHistory(String patronId) {
        return findPatronById(patronId)
                .map(Patron::getBorrowedBooksHistory)
                .orElse(Collections.emptyList());
    }
}
