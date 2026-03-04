package org.lms.service;

import org.lms.entity.BookTransfer;
import org.lms.entity.Branch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Manages multiple library branches and book transfers between them.
 * Single Responsibility: Coordinate multi-branch operations.
 */
public class BranchManager {
    private static final Logger log = LoggerFactory.getLogger(BranchManager.class);
    
    private final Map<String, Branch> branches = new ConcurrentHashMap<>();
    private final Map<String, BranchInventory> branchInventories = new ConcurrentHashMap<>();
    private final Map<String, BookTransfer> transfers = new ConcurrentHashMap<>();
    private final AtomicInteger transferCounter = new AtomicInteger(1);
    private final InventoryManager globalInventoryManager;

    public BranchManager(InventoryManager globalInventoryManager) {
        this.globalInventoryManager = globalInventoryManager;
    }

    /**
     * Register a new branch.
     */
    public synchronized void addBranch(Branch branch) {
        branches.put(branch.getId(), branch);
        branchInventories.put(branch.getId(), new BranchInventory(branch.getId()));
        log.info("Branch registered: {}", branch);
    }

    /**
     * Remove a branch (must be empty).
     */
    public synchronized boolean removeBranch(String branchId) {
        BranchInventory inventory = branchInventories.get(branchId);
        if (inventory != null && inventory.getTotalCopies() > 0) {
            log.warn("Cannot remove branch {} - still has {} books", branchId, inventory.getTotalCopies());
            return false;
        }
        
        branches.remove(branchId);
        branchInventories.remove(branchId);
        log.info("Branch removed: {}", branchId);
        return true;
    }

    /**
     * Update branch information.
     */
    public synchronized void updateBranch(Branch branch) {
        branches.put(branch.getId(), branch);
        log.info("Branch updated: {}", branch);
    }

    /**
     * Get a branch by ID.
     */
    public Optional<Branch> findBranchById(String branchId) {
        return Optional.ofNullable(branches.get(branchId));
    }

    /**
     * Get all branches.
     */
    public List<Branch> getAllBranches() {
        return new ArrayList<>(branches.values());
    }

    /**
     * Add book copies to a specific branch.
     */
    public synchronized void addBookToBranch(String branchId, String isbn, int copies) {
        if (!branches.containsKey(branchId)) {
            throw new IllegalArgumentException("Branch not found: " + branchId);
        }
        if (!globalInventoryManager.hasBook(isbn)) {
            throw new IllegalArgumentException("Book not in catalog: " + isbn);
        }
        
        BranchInventory inventory = branchInventories.get(branchId);
        inventory.addCopies(isbn, copies);
        log.info("Added {} copies of {} to branch {}", copies, isbn, branchId);
    }

    /**
     * Remove book copies from a specific branch.
     */
    public synchronized boolean removeBookFromBranch(String branchId, String isbn, int copies) {
        BranchInventory inventory = branchInventories.get(branchId);
        if (inventory == null) {
            log.warn("Branch not found: {}", branchId);
            return false;
        }
        return inventory.removeCopies(isbn, copies);
    }

    /**
     * Get the number of copies at a specific branch.
     */
    public int getBookCountAtBranch(String branchId, String isbn) {
        BranchInventory inventory = branchInventories.get(branchId);
        return inventory != null ? inventory.getCopyCount(isbn) : 0;
    }

    /**
     * Check if a book is available at a specific branch.
     */
    public boolean isBookAvailableAtBranch(String branchId, String isbn) {
        return getBookCountAtBranch(branchId, isbn) > 0;
    }

    /**
     * Find all branches that have a specific book.
     */
    public List<Branch> findBranchesWithBook(String isbn) {
        return branchInventories.entrySet().stream()
                .filter(entry -> entry.getValue().hasBook(isbn))
                .map(entry -> branches.get(entry.getKey()))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    /**
     * Transfer a book copy from one branch to another.
     * 
     * @return Transfer ID if successful, empty if failed
     */
    public synchronized Optional<String> transferBook(String isbn, String fromBranchId, String toBranchId) {
        // Validation
        if (!branches.containsKey(fromBranchId)) {
            log.warn("Transfer failed: Source branch not found - {}", fromBranchId);
            return Optional.empty();
        }
        if (!branches.containsKey(toBranchId)) {
            log.warn("Transfer failed: Destination branch not found - {}", toBranchId);
            return Optional.empty();
        }
        if (fromBranchId.equals(toBranchId)) {
            log.warn("Transfer failed: Source and destination are the same");
            return Optional.empty();
        }
        
        BranchInventory fromInventory = branchInventories.get(fromBranchId);
        if (!fromInventory.hasBook(isbn)) {
            log.warn("Transfer failed: Book {} not available at branch {}", isbn, fromBranchId);
            return Optional.empty();
        }
        
        // Create transfer record
        String transferId = "T" + transferCounter.getAndIncrement();
        BookTransfer transfer = new BookTransfer(transferId, isbn, fromBranchId, toBranchId);
        transfers.put(transferId, transfer);
        
        // Execute transfer
        fromInventory.removeCopies(isbn, 1);
        transfer.setStatus(BookTransfer.TransferStatus.IN_TRANSIT);
        
        BranchInventory toInventory = branchInventories.get(toBranchId);
        toInventory.addCopies(isbn, 1);
        transfer.setStatus(BookTransfer.TransferStatus.COMPLETED);
        
        log.info("Book transfer completed: {}", transfer);
        return Optional.of(transferId);
    }

    /**
     * Get transfer details by ID.
     */
    public Optional<BookTransfer> getTransfer(String transferId) {
        return Optional.ofNullable(transfers.get(transferId));
    }

    /**
     * Get all transfers.
     */
    public List<BookTransfer> getAllTransfers() {
        return new ArrayList<>(transfers.values());
    }

    /**
     * Get transfers for a specific branch (sent or received).
     */
    public List<BookTransfer> getTransfersForBranch(String branchId) {
        return transfers.values().stream()
                .filter(t -> t.getFromBranchId().equals(branchId) || 
                            t.getToBranchId().equals(branchId))
                .collect(Collectors.toList());
    }

    /**
     * Get inventory for a specific branch.
     */
    public Optional<BranchInventory> getBranchInventory(String branchId) {
        return Optional.ofNullable(branchInventories.get(branchId));
    }

    /**
     * Get total copies of a book across all branches.
     */
    public int getTotalCopiesAcrossAllBranches(String isbn) {
        return branchInventories.values().stream()
                .mapToInt(inv -> inv.getCopyCount(isbn))
                .sum();
    }

    /**
     * Check if the system has any branch.
     */
    public boolean hasBranches() {
        return !branches.isEmpty();
    }

    /**
     * Get the number of registered branches.
     */
    public int getBranchCount() {
        return branches.size();
    }
}
