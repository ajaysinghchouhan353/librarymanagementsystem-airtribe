package org.lms.service;

import org.lms.entity.Book;
import org.lms.entity.BookTransfer;
import org.lms.entity.Branch;
import org.lms.entity.Loan;
import org.lms.entity.Patron;
import org.lms.observer.BookObserver;
import org.lms.observer.ReservationManager;
import org.lms.recommendation.RecommendationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Facade for the library management system.
 * Coordinates between different managers following SOLID principles.
 * Dependency Inversion: Depends on manager abstractions.
 */
public class LibraryService {
    private static final Logger log = LoggerFactory.getLogger(LibraryService.class);

    private final InventoryManager inventoryManager;
    private final PatronManager patronManager;
    private final LoanManager loanManager;
    private final ReservationManager reservationManager;
    private final BranchManager branchManager;
    private RecommendationStrategy recommendationStrategy;

    public LibraryService() {
        this.inventoryManager = new InventoryManager();
        this.patronManager = new PatronManager();
        this.reservationManager = new ReservationManager();
        this.loanManager = new LoanManager(inventoryManager, reservationManager);
        this.branchManager = new BranchManager(inventoryManager);
    }

    // Constructor for dependency injection (Open/Closed Principle)
    public LibraryService(InventoryManager inventoryManager, 
                         PatronManager patronManager,
                         LoanManager loanManager,
                         ReservationManager reservationManager,
                         BranchManager branchManager) {
        this.inventoryManager = inventoryManager;
        this.patronManager = patronManager;
        this.loanManager = loanManager;
        this.reservationManager = reservationManager;
        this.branchManager = branchManager;
    }

    public void setRecommendationStrategy(RecommendationStrategy strategy) {
        this.recommendationStrategy = strategy;
    }

    // ========== Book Management (Delegates to InventoryManager) ==========
    
    public void addBook(Book book) {
        inventoryManager.addBook(book);
    }

    public void removeBook(String isbn) {
        inventoryManager.removeBook(isbn);
    }

    public void updateBook(Book book) {
        inventoryManager.updateBook(book);
    }

    public Optional<Book> findByIsbn(String isbn) {
        return inventoryManager.findByIsbn(isbn);
    }

    public List<Book> findByTitle(String title) {
        return inventoryManager.findByTitle(title);
    }

    public List<Book> findByAuthor(String author) {
        return inventoryManager.findByAuthor(author);
    }

    public List<Book> getAllBooks() {
        return inventoryManager.getAllBooks();
    }

    // ========== Patron Management (Delegates to PatronManager) ==========
    
    public void addPatron(Patron patron) {
        patronManager.addPatron(patron);
    }

    public void removePatron(Patron patron) {
        patronManager.removePatron(patron.getId());
    }

    public void updatePatron(Patron patron) {
        patronManager.updatePatron(patron);
    }

    public Optional<Patron> findPatronById(String id) {
        return patronManager.findPatronById(id);
    }

    // ========== Lending Process (Coordinates between managers) ==========
    
    public boolean checkoutBook(String isbn, String patronId) {
        if (!patronManager.hasPatron(patronId)) {
            log.warn("Checkout failed: Patron not found - {}", patronId);
            return false;
        }
        
        boolean success = loanManager.checkoutBook(isbn, patronId);
        if (success) {
            patronManager.recordBorrow(patronId, isbn);
        }
        return success;
    }

    public boolean returnBook(String isbn) {
        return loanManager.returnBook(isbn);
    }

    public boolean isAvailable(String isbn) {
        return loanManager.isAvailable(isbn);
    }

    public List<Loan> getLoanHistory() {
        return loanManager.getLoanHistory();
    }

    // ========== Reservation System (Delegates to ReservationManager) ==========
    
    public void reserveBook(String isbn, BookObserver observer) {
        if (!inventoryManager.hasBook(isbn)) {
            log.warn("Reservation failed: Book not found - {}", isbn);
            return;
        }
        reservationManager.reserveBook(isbn, observer);
    }

    // ========== Recommendation System (Uses Strategy Pattern) ==========
    
    public List<Book> recommendedBooks(String patronId) {
        if (recommendationStrategy == null) {
            log.warn("No recommendation strategy set");
            return Collections.emptyList();
        }
        
        List<String> patronHistory = patronManager.getPatronHistory(patronId);
        if (patronHistory.isEmpty()) {
            log.warn("Recommendation failed: No borrowing history for patron - {}", patronId);
            return Collections.emptyList();
        }
        
        return recommendationStrategy.recommendBooks(patronId, patronHistory, getAllBooks());
    }

    // ========== Manager Access (For advanced use cases) ==========
    
    public InventoryManager getInventoryManager() {
        return inventoryManager;
    }

    public PatronManager getPatronManager() {
        return patronManager;
    }

    public LoanManager getLoanManager() {
        return loanManager;
    }

    public ReservationManager getReservationManager() {
        return reservationManager;
    }

    public BranchManager getBranchManager() {
        return branchManager;
    }

    // ========== Multi-Branch Support (Delegates to BranchManager) ==========
    
    public void addBranch(Branch branch) {
        branchManager.addBranch(branch);
    }

    public boolean removeBranch(String branchId) {
        return branchManager.removeBranch(branchId);
    }

    public void updateBranch(Branch branch) {
        branchManager.updateBranch(branch);
    }

    public Optional<Branch> findBranchById(String branchId) {
        return branchManager.findBranchById(branchId);
    }

    public List<Branch> getAllBranches() {
        return branchManager.getAllBranches();
    }

    public void addBookToBranch(String branchId, String isbn, int copies) {
        branchManager.addBookToBranch(branchId, isbn, copies);
    }

    public boolean removeBookFromBranch(String branchId, String isbn, int copies) {
        return branchManager.removeBookFromBranch(branchId, isbn, copies);
    }

    public int getBookCountAtBranch(String branchId, String isbn) {
        return branchManager.getBookCountAtBranch(branchId, isbn);
    }

    public boolean isBookAvailableAtBranch(String branchId, String isbn) {
        return branchManager.isBookAvailableAtBranch(branchId, isbn);
    }

    public List<Branch> findBranchesWithBook(String isbn) {
        return branchManager.findBranchesWithBook(isbn);
    }

    public Optional<String> transferBook(String isbn, String fromBranchId, String toBranchId) {
        return branchManager.transferBook(isbn, fromBranchId, toBranchId);
    }

    public Optional<BookTransfer> getTransfer(String transferId) {
        return branchManager.getTransfer(transferId);
    }

    public List<BookTransfer> getAllTransfers() {
        return branchManager.getAllTransfers();
    }

    public List<BookTransfer> getTransfersForBranch(String branchId) {
        return branchManager.getTransfersForBranch(branchId);
    }

    public int getTotalCopiesAcrossAllBranches(String isbn) {
        return branchManager.getTotalCopiesAcrossAllBranches(isbn);
    }
}
