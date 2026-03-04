package org.lms.service;

import org.lms.entity.Loan;
import org.lms.observer.ReservationManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;

/**
 * Manages loan operations including checkout and return.
 * Single Responsibility: Handle book lending lifecycle.
 */
public class LoanManager {
    private static final Logger log = LoggerFactory.getLogger(LoanManager.class);
    
    private final Map<String, Loan> activeLoans = new HashMap<>(); // isbn -> Loan
    private final List<Loan> loanHistory = new ArrayList<>();
    private final InventoryManager inventoryManager;
    private final ReservationManager reservationManager;

    public LoanManager(InventoryManager inventoryManager, ReservationManager reservationManager) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
    }

    /**
     * Checkout a book to a patron.
     * 
     * @return true if checkout successful, false otherwise
     */
    public synchronized boolean checkoutBook(String isbn, String patronId) {
        if (!inventoryManager.hasBook(isbn)) {
            log.warn("Checkout failed: Book not found - {}", isbn);
            return false;
        }
        if (activeLoans.containsKey(isbn)) {
            log.warn("Checkout failed: Book already loaned - {}", isbn);
            return false;
        }
        
        Loan loan = new Loan(isbn, patronId, LocalDate.now());
        activeLoans.put(isbn, loan);
        loanHistory.add(loan);
        log.info("Book checked out: ISBN={} to PatronID={}", isbn, patronId);
        return true;
    }

    /**
     * Return a book and notify any reservations.
     * 
     * @return true if return successful, false otherwise
     */
    public synchronized boolean returnBook(String isbn) {
        Loan loan = activeLoans.remove(isbn);
        if (loan == null) {
            log.warn("Return failed: Book not currently loaned - {}", isbn);
            return false;
        }
        loan.markReturned();
        log.info("Book returned: {}", isbn);
        
        // Notify reservation system
        inventoryManager.findByIsbn(isbn).ifPresent(book -> 
            reservationManager.notifyBookAvailable(book)
        );
        
        return true;
    }

    /**
     * Check if a book is currently available (not loaned out).
     */
    public synchronized boolean isAvailable(String isbn) {
        return inventoryManager.hasBook(isbn) && !activeLoans.containsKey(isbn);
    }

    /**
     * Get all active loans.
     */
    public Map<String, Loan> getActiveLoans() {
        return Collections.unmodifiableMap(activeLoans);
    }

    /**
     * Get complete loan history.
     */
    public List<Loan> getLoanHistory() {
        return Collections.unmodifiableList(loanHistory);
    }

    /**
     * Get active loan for a specific book.
     */
    public Optional<Loan> getActiveLoan(String isbn) {
        return Optional.ofNullable(activeLoans.get(isbn));
    }

    /**
     * Get all loans for a specific patron.
     */
    public List<Loan> getPatronLoans(String patronId) {
        return loanHistory.stream()
                .filter(loan -> loan.getPatronId().equals(patronId))
                .toList();
    }
}
