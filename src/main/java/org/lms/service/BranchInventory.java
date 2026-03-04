package org.lms.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Manages book inventory for a specific branch.
 * Single Responsibility: Track book copies at one branch location.
 */
public class BranchInventory {
    private static final Logger log = LoggerFactory.getLogger(BranchInventory.class);
    
    private final String branchId;
    private final Map<String, Integer> bookCopies = new HashMap<>(); // isbn -> count

    public BranchInventory(String branchId) {
        this.branchId = branchId;
    }

    /**
     * Add copies of a book to this branch.
     */
    public synchronized void addCopies(String isbn, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        bookCopies.merge(isbn, count, Integer::sum);
        log.info("Added {} copies of {} to branch {}", count, isbn, branchId);
    }

    /**
     * Remove copies of a book from this branch.
     */
    public synchronized boolean removeCopies(String isbn, int count) {
        if (count <= 0) {
            throw new IllegalArgumentException("Count must be positive");
        }
        
        Integer current = bookCopies.get(isbn);
        if (current == null || current < count) {
            log.warn("Cannot remove {} copies of {} from branch {} (available: {})", 
                    count, isbn, branchId, current);
            return false;
        }
        
        int newCount = current - count;
        if (newCount == 0) {
            bookCopies.remove(isbn);
        } else {
            bookCopies.put(isbn, newCount);
        }
        
        log.info("Removed {} copies of {} from branch {}", count, isbn, branchId);
        return true;
    }

    /**
     * Get the number of copies of a book at this branch.
     */
    public int getCopyCount(String isbn) {
        return bookCopies.getOrDefault(isbn, 0);
    }

    /**
     * Check if this branch has at least one copy of a book.
     */
    public boolean hasBook(String isbn) {
        return getCopyCount(isbn) > 0;
    }

    /**
     * Get all ISBNs with copies at this branch.
     */
    public Set<String> getAllBookIsbns() {
        return new HashSet<>(bookCopies.keySet());
    }

    /**
     * Get total number of books (all copies) at this branch.
     */
    public int getTotalCopies() {
        return bookCopies.values().stream().mapToInt(Integer::intValue).sum();
    }

    public String getBranchId() {
        return branchId;
    }

    /**
     * Get a copy of the inventory state.
     */
    public Map<String, Integer> getInventorySnapshot() {
        return new HashMap<>(bookCopies);
    }
}
