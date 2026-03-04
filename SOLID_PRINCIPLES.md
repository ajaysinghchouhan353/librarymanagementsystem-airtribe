# SOLID Principles Implementation

This document explains how the Library Management System demonstrates SOLID principles through its architecture.

## Overview of SOLID Principles

The refactoring separated concerns into dedicated manager classes, transforming the monolithic `LibraryService` into a clean facade pattern.

---

## 1. Single Responsibility Principle (SRP)

**Before:** `LibraryService` handled everything (book inventory, patron management, loans, reservations, recommendations).

**After:** Each class has one clear responsibility:

- **`InventoryManager`**: Manages book catalog and availability
- **`PatronManager`**: Handles patron registration and information
- **`LoanManager`**: Controls checkout/return lifecycle
- **`ReservationManager`**: Manages book reservations and notifications
- **`LibraryService`**: Orchestrates operations (Facade pattern)

### Benefits:
- Easier to understand and maintain
- Each class has one reason to change
- Better testability (can test each manager independently)

---

## 2. Open/Closed Principle (OCP)

**Extension without modification:**

```java
// LibraryService supports dependency injection
public LibraryService(InventoryManager inventoryManager, 
                     PatronManager patronManager,
                     LoanManager loanManager,
                     ReservationManager reservationManager) {
    // ...
}
```

- Can extend functionality by creating new managers
- Can inject mock managers for testing
- Can add new features without modifying existing code

**Strategy Pattern for Recommendations:**
```java
private RecommendationStrategy recommendationStrategy;

public void setRecommendationStrategy(RecommendationStrategy strategy) {
    this.recommendationStrategy = strategy;
}
```

- New recommendation algorithms can be added without changing LibraryService
- Currently has `BasicRecommendationStrategy`, can add `MLRecommendationStrategy`, etc.

---

## 3. Liskov Substitution Principle (LSP)

**Observer Pattern Implementation:**
```java
public interface BookObserver {
    void onBookAvailable(Book book);
}
```

- Any implementation of `BookObserver` can be substituted
- `ReservationManager` works with any observer implementation

**Strategy Pattern:**
```java
public interface RecommendationStrategy {
    List<Book> recommendBooks(String patronId, List<String> patronHistory, List<Book> inventory);
}
```

- Any `RecommendationStrategy` implementation is interchangeable
- System behavior remains correct regardless of which strategy is used

---

## 4. Interface Segregation Principle (ISP)

**Focused Interfaces:**

- `BookObserver`: Only one method - `onBookAvailable(Book book)`
- `RecommendationStrategy`: Only one method - `recommendBooks(...)`

**Manager Segregation:**
- Each manager exposes only relevant methods for its domain
- Clients depend only on the methods they use
- No "fat interfaces" forcing unnecessary dependencies

---

## 5. Dependency Inversion Principle (DIP)

**High-level modules depend on abstractions:**

```java
public class LibraryService {
    private RecommendationStrategy recommendationStrategy;  // Abstraction
    // ...
}
```

```java
public class LoanManager {
    private final InventoryManager inventoryManager;
    private final ReservationManager reservationManager;
    
    public LoanManager(InventoryManager inventoryManager, 
                      ReservationManager reservationManager) {
        this.inventoryManager = inventoryManager;
        this.reservationManager = reservationManager;
    }
}
```

**Benefits:**
- `LoanManager` depends on `InventoryManager` and `ReservationManager` (dependencies injected)
- Easy to mock for testing
- Can swap implementations without changing dependent code

---

## Design Patterns Summary

1. **Facade Pattern**: `LibraryService` provides a simplified interface to complex subsystems
2. **Observer Pattern**: `BookObserver` and `ReservationManager` for event notifications
3. **Strategy Pattern**: `RecommendationStrategy` for pluggable algorithms
4. **Factory Pattern**: `LibraryFactory` for object creation

---

## Class Diagram

```
┌─────────────────┐
│ LibraryService  │ (Facade)
│  (Coordinator)  │
└────────┬────────┘
         │ uses
         ├─────────────┬──────────────┬─────────────────┬──────────────────┐
         │             │              │                 │                  │
┌────────▼────────┐ ┌──▼──────────┐ ┌▼─────────────┐ ┌─▼─────────────────┐ │
│InventoryManager│ │PatronManager│ │ LoanManager  │ │ReservationManager │ │
│                 │ │             │ │              │ │                   │ │
│ - Book catalog  │ │ - Patron DB │ │ - Checkouts  │ │ - Reservations    │ │
│ - Search        │ │ - History   │ │ - Returns    │ │ - Notifications   │ │
└─────────────────┘ └─────────────┘ └──────────────┘ └───────────────────┘ │
                                                                             │
                                            ┌────────────────────────────────▼┐
                                            │  RecommendationStrategy          │
                                            │  (Strategy Pattern Interface)    │
                                            └──────────────────────────────────┘
```

---

## Testing Advantages

With SOLID principles:

1. **Unit Testing**: Each manager can be tested independently
2. **Mocking**: Dependencies can be easily mocked
3. **Integration Testing**: Test component interactions through LibraryService facade
4. **Test Coverage**: Smaller, focused classes are easier to achieve 100% coverage

---

## Future Extensions (Examples)

Thanks to OCP and DIP:

- Add `BranchManager` for multi-branch support
- Implement `PremiumRecommendationStrategy` with ML
- Create `LateFeeCalculator` for overdue books
- Add `NotificationService` for email/SMS alerts
- Implement `AuditLogger` for compliance tracking

All without modifying existing, tested code!
