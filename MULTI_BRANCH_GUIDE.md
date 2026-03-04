# Multi-Branch Support Documentation

## Overview

The Library Management System supports **multiple library branches** with independent inventories and seamless book transfers between locations. This feature enables real-world library network operations while maintaining SOLID principles.

---

## Architecture

### Components

```
┌─────────────────────────────────────────────────────┐
│              LibraryService (Facade)                │
└────────────────────┬────────────────────────────────┘
                     │
         ┌───────────▼──────────┐
         │   BranchManager      │
         │                      │
         │  Responsibilities:   │
         │  - Branch registry   │
         │  - Transfers         │
         │  - Cross-branch ops  │
         └───────────┬──────────┘
                     │
      ┌──────────────┼──────────────┐
      │              │              │
┌─────▼──────┐ ┌─────▼──────┐ ┌────▼───────┐
│ Branch 001 │ │ Branch 002 │ │ Branch 003 │
│ Inventory  │ │ Inventory  │ │ Inventory  │
│            │ │            │ │            │
│ Book A: 5  │ │ Book A: 2  │ │ Book B: 6  │
│ Book B: 3  │ │ Book C: 4  │ │ Book C: 1  │
└────────────┘ └────────────┘ └────────────┘
```

### Key Classes

#### 1. **Branch** (Entity)
Represents a physical library location.

```java
public class Branch {
    private final String id;
    private String name;
    private String address;
    private String phoneNumber;
}
```

**Attributes:**
- `id`: Unique branch identifier (immutable)
- `name`: Branch display name
- `address`: Physical location
- `phoneNumber`: Contact number

#### 2. **BranchInventory** (Service)
Manages book copies at a specific branch.

**Key Methods:**
- `addCopies(isbn, count)` - Add book copies to this branch
- `removeCopies(isbn, count)` - Remove copies from this branch
- `getCopyCount(isbn)` - Get copies available
- `hasBook(isbn)` - Check if book is available
- `getTotalCopies()` - Total books at this branch

**Responsibility:** Track inventory for ONE branch (Single Responsibility Principle)

#### 3. **BranchManager** (Service)
Coordinates all branch operations.

**Key Methods:**
- `addBranch(branch)` - Register new branch
- `removeBranch(branchId)` - Deregister branch
- `addBookToBranch(branchId, isbn, copies)` - Add books to branch
- `transferBook(isbn, fromBranch, toBranch)` - Transfer between branches
- `findBranchesWithBook(isbn)` - Locate book across system
- `getTotalCopiesAcrossAllBranches(isbn)` - System-wide count

**Responsibility:** Multi-branch coordination (Single Responsibility Principle)

#### 4. **BookTransfer** (Entity)
Records book movement between branches.

```java
public class BookTransfer {
    private final String transferId;
    private final String isbn;
    private final String fromBranchId;
    private final String toBranchId;
    private final LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private TransferStatus status; // PENDING, IN_TRANSIT, COMPLETED, CANCELLED
}
```

**Purpose:** Complete audit trail of book movements

---

## Usage Examples

### 1. Create Branches

```java
Branch downtown = LibraryFactory.createBranch(
    "BRANCH-001", 
    "Downtown Branch", 
    "123 Main St, Downtown", 
    "555-0101"
);

Branch uptown = LibraryFactory.createBranch(
    "BRANCH-002", 
    "Uptown Branch", 
    "456 High St, Uptown"
);

libraryService.addBranch(downtown);
libraryService.addBranch(uptown);
```

### 2. Add Books to Specific Branches

```java
// Add 5 copies of "Effective Java" to Downtown
libraryService.addBookToBranch("BRANCH-001", "978-0134685991", 5);

// Add 3 copies to Uptown
libraryService.addBookToBranch("BRANCH-002", "978-0134685991", 3);
```

### 3. Check Book Availability

```java
// Copies at specific branch
int copies = libraryService.getBookCountAtBranch("BRANCH-001", "978-0134685991");
logger.info("Downtown has {} copies", copies);

// Total across all branches
int total = libraryService.getTotalCopiesAcrossAllBranches("978-0134685991");
logger.info("System-wide total: {}", total);

// Is available at branch?
boolean available = libraryService.isBookAvailableAtBranch("BRANCH-001", "978-0134685991");
```

### 4. Find All Branches with a Book

```java
List<Branch> branches = libraryService.findBranchesWithBook("978-0134685991");
logger.info("Book available at {} branches", branches.size());

branches.forEach(branch -> 
    logger.info("- {} at {}", branch.getName(), branch.getAddress())
);
```

### 5. Transfer Books Between Branches

```java
// Transfer one copy from Downtown to Uptown
Optional<String> transferId = libraryService.transferBook(
    "978-0134685991",    // ISBN
    "BRANCH-001",        // From Downtown
    "BRANCH-002"         // To Uptown
);

transferId.ifPresent(id -> {
    logger.info("Transfer successful! ID: {}", id);
    
    // Get transfer details
    libraryService.getTransfer(id).ifPresent(transfer -> {
        logger.info("Status: {}", transfer.getStatus());
        logger.info("Requested: {}", transfer.getRequestedAt());
        logger.info("Completed: {}", transfer.getCompletedAt());
    });
});
```

### 6. View All Transfers

```java
// All transfers in the system
List<BookTransfer> allTransfers = libraryService.getAllTransfers();
logger.info("Total transfers: {}", allTransfers.size());

// Transfers for a specific branch
List<BookTransfer> branchTransfers = 
    libraryService.getTransfersForBranch("BRANCH-001");
logger.info("Downtown transfers: {}", branchTransfers.size());
```

### 7. Branch Management

```java
// Get all branches
List<Branch> allBranches = libraryService.getAllBranches();
logger.info("Total branches: {}", allBranches.size());

// Find specific branch
libraryService.findBranchById("BRANCH-001").ifPresent(branch -> {
    logger.info("Found: {}", branch);
    
    // Update branch info
    branch.setPhoneNumber("555-9999");
    libraryService.updateBranch(branch);
});

// Remove branch (must be empty)
boolean removed = libraryService.removeBranch("BRANCH-003");
```

---

## Business Use Cases

### Use Case 1: Patron Requests Book at Different Branch

**Scenario:** Patron at Downtown wants a book only available at Uptown.

```java
String isbn = "978-0134685991";
String patronBranch = "BRANCH-001"; // Downtown
String patronId = "P123";

// Check if book available at patron's branch
if (!libraryService.isBookAvailableAtBranch(patronBranch, isbn)) {
    // Find which branches have it
    List<Branch> branches = libraryService.findBranchesWithBook(isbn);
    
    if (!branches.isEmpty()) {
        Branch sourceBranch = branches.get(0);
        logger.info("Book found at {}, initiating transfer...", sourceBranch.getName());
        
        // Transfer book
        libraryService.transferBook(isbn, sourceBranch.getId(), patronBranch)
            .ifPresent(transferId -> {
                logger.info("Transfer ID: {}. Book will arrive soon!", transferId);
                // Could notify patron here
            });
    }
}
```

### Use Case 2: Inventory Balancing

**Scenario:** Distribute books evenly across branches.

```java
String isbn = "978-0134685991";
int totalCopies = libraryService.getTotalCopiesAcrossAllBranches(isbn);
List<Branch> branches = libraryService.getAllBranches();
int targetPerBranch = totalCopies / branches.size();

for (Branch branch : branches) {
    int current = libraryService.getBookCountAtBranch(branch.getId(), isbn);
    int needed = targetPerBranch - current;
    
    if (needed > 0) {
        // Find branch with surplus
        for (Branch donor : branches) {
            int surplus = libraryService.getBookCountAtBranch(donor.getId(), isbn) - targetPerBranch;
            if (surplus > 0) {
                int transferCount = Math.min(needed, surplus);
                for (int i = 0; i < transferCount; i++) {
                    libraryService.transferBook(isbn, donor.getId(), branch.getId());
                }
                needed -= transferCount;
                if (needed == 0) break;
            }
        }
    }
}
```

### Use Case 3: Branch Performance Report

**Scenario:** Generate report for each branch.

```java
for (Branch branch : libraryService.getAllBranches()) {
    logger.info("=== {} Report ===", branch.getName());
    
    BranchInventory inventory = libraryService.getBranchManager()
        .getBranchInventory(branch.getId()).get();
    
    logger.info("Total books: {}", inventory.getTotalCopies());
    logger.info("Unique titles: {}", inventory.getAllBookIsbns().size());
    
    // Transfers sent/received
    List<BookTransfer> transfers = libraryService.getTransfersForBranch(branch.getId());
    long sent = transfers.stream()
        .filter(t -> t.getFromBranchId().equals(branch.getId()))
        .count();
    long received = transfers.stream()
        .filter(t -> t.getToBranchId().equals(branch.getId()))
        .count();
    
    logger.info("Transfers sent: {}, received: {}", sent, received);
}
```

---

## SOLID Principles in Multi-Branch Design

### Single Responsibility Principle (SRP) ✅
- **BranchInventory**: Manages copies at ONE branch only
- **BranchManager**: Coordinates multi-branch operations only
- **BookTransfer**: Records ONE transfer event only

### Open/Closed Principle (OCP) ✅
- Can add new branch types (e.g., `MobileBranch`, `DigitalBranch`) without modifying existing code
- Transfer status can be extended with new states

### Liskov Substitution Principle (LSP) ✅
- All `BranchInventory` instances behave consistently
- Can substitute branch implementations transparently

### Interface Segregation Principle (ISP) ✅
- `BranchInventory` has focused interface (inventory operations only)
- `BranchManager` has focused interface (coordination only)
- No fat interfaces forcing unnecessary dependencies

### Dependency Inversion Principle (DIP) ✅
- `BranchManager` depends on `InventoryManager` (injected dependency)
- High-level `LibraryService` depends on abstractions
- Easy to mock for testing

---

## Thread Safety

All branch operations are **thread-safe**:

- `BranchInventory`: Synchronized methods for copy management
- `BranchManager`: ConcurrentHashMap for branch storage
- `BookTransfer`: Atomic transfer operations

Safe for concurrent operations in multi-threaded environments.

---

## Future Enhancements

Possible extensions following Open/Closed Principle:

1. **Branch Hierarchies**: Main branch + sub-branches
2. **Transfer Rules**: Approval workflows, distance-based rules
3. **Branch Statistics**: Analytics per branch
4. **Mobile Branches**: Support for bookmobiles
5. **Digital Branches**: E-book distribution points
6. **Branch Hours**: Operating hours and availability
7. **Staff Management**: Employees per branch

All can be added without modifying existing code! 🚀

---

## Testing Multi-Branch Features

### Unit Test Example

```java
@Test
public void testBookTransfer() {
    InventoryManager inventory = new InventoryManager();
    BranchManager branchManager = new BranchManager(inventory);
    
    // Setup
    Book book = new Book("123", "Test Book", "Author", 2024);
    inventory.addBook(book);
    
    Branch b1 = new Branch("B1", "Branch 1", "Address 1");
    Branch b2 = new Branch("B2", "Branch 2", "Address 2");
    branchManager.addBranch(b1);
    branchManager.addBranch(b2);
    
    branchManager.addBookToBranch("B1", "123", 5);
    
    // Execute
    Optional<String> transferId = branchManager.transferBook("123", "B1", "B2");
    
    // Verify
    assertTrue(transferId.isPresent());
    assertEquals(4, branchManager.getBookCountAtBranch("B1", "123"));
    assertEquals(1, branchManager.getBookCountAtBranch("B2", "123"));
}
```

---

## Summary

The multi-branch support feature provides:

- ✅ **Multiple branch management** with independent inventories
- ✅ **Book transfers** with complete audit trail
- ✅ **Cross-branch search** and analytics
- ✅ **Thread-safe operations** for concurrent access
- ✅ **SOLID principles** throughout design
- ✅ **Easy extension** for future features

**Perfect for real-world library networks! 📚🏢**
