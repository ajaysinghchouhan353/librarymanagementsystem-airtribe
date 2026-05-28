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
    
    private final Map<String, Deque<Loan>> activeLoans = new HashMap<>(); // key -> loans
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
        String key = buildLoanKey(isbn, null);
        Deque<Loan> loans = activeLoans.get(key);
        if (loans != null && !loans.isEmpty()) {
            log.warn("Checkout failed: Book already loaned - {}", isbn);
            return false;
        }
        
        Loan loan = new Loan(isbn, patronId, LocalDate.now());
        addActiveLoan(key, loan);
        loanHistory.add(loan);
        log.info("Book checked out: ISBN={} to PatronID={}", isbn, patronId);
        return true;
    }

    /**
     * Checkout a book from a specific branch.
     * Branch inventory availability must be validated by the caller.
     */
    public synchronized boolean checkoutBookAtBranch(String isbn, String patronId, String branchId) {
        if (!inventoryManager.hasBook(isbn)) {
            log.warn("Branch checkout failed: Book not found - {}", isbn);
            return false;
        }

        Loan loan = new Loan(isbn, patronId, LocalDate.now(), branchId);
        addActiveLoan(buildLoanKey(isbn, branchId), loan);
        loanHistory.add(loan);
        log.info("Book checked out: ISBN={} to PatronID={} at BranchID={}", isbn, patronId, branchId);
        return true;
    }

    /**
     * Return a book and notify any reservations.
     * 
     * @return true if return successful, false otherwise
     */
    public synchronized boolean returnBook(String isbn) {
        Loan loan = removeActiveLoan(buildLoanKey(isbn, null));
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
     * Return a book to a specific branch.
     */
    public synchronized boolean returnBookAtBranch(String isbn, String branchId) {
        Loan loan = removeActiveLoan(buildLoanKey(isbn, branchId));
        if (loan == null) {
            log.warn("Branch return failed: Book not currently loaned - {} at {}", isbn, branchId);
            return false;
        }
        loan.markReturned();
        log.info("Book returned: {} to BranchID={}", isbn, branchId);

        inventoryManager.findByIsbn(isbn).ifPresent(book ->
            reservationManager.notifyBookAvailable(book)
        );

        return true;
    }

    /**
     * Check if a book is currently available (not loaned out).
     */
    public synchronized boolean isAvailable(String isbn) {
        Deque<Loan> loans = activeLoans.get(buildLoanKey(isbn, null));
        return inventoryManager.hasBook(isbn) && (loans == null || loans.isEmpty());
    }

    /**
     * Get all active loans.
     */
    public Map<String, List<Loan>> getActiveLoans() {
        Map<String, List<Loan>> snapshot = new HashMap<>();
        for (Map.Entry<String, Deque<Loan>> entry : activeLoans.entrySet()) {
            snapshot.put(entry.getKey(), List.copyOf(entry.getValue()));
        }
        return Collections.unmodifiableMap(snapshot);
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
        return Optional.ofNullable(getFirstActiveLoan(buildLoanKey(isbn, null)));
    }

    /**
     * Get active loan for a specific book at a branch.
     */
    public Optional<Loan> getActiveLoan(String isbn, String branchId) {
        return Optional.ofNullable(getFirstActiveLoan(buildLoanKey(isbn, branchId)));
    }

    /**
     * Get all loans for a specific patron.
     */
    public List<Loan> getPatronLoans(String patronId) {
        return loanHistory.stream()
                .filter(loan -> loan.getPatronId().equals(patronId))
                .toList();
    }

    private static String buildLoanKey(String isbn, String branchId) {
        return branchId == null ? isbn : isbn + "@" + branchId;
    }

    private void addActiveLoan(String key, Loan loan) {
        activeLoans.computeIfAbsent(key, k -> new ArrayDeque<>()).add(loan);
    }

    private Loan removeActiveLoan(String key) {
        Deque<Loan> loans = activeLoans.get(key);
        if (loans == null || loans.isEmpty()) {
            return null;
        }
        Loan loan = loans.poll();
        if (loans.isEmpty()) {
            activeLoans.remove(key);
        }
        return loan;
    }

    private Loan getFirstActiveLoan(String key) {
        Deque<Loan> loans = activeLoans.get(key);
        return loans == null ? null : loans.peek();
    }
}
