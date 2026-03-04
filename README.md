# Library Management System - Airtribe

A well-architected Library Management System demonstrating **SOLID principles**, design patterns, and clean code practices in Java.

## 🏗️ Architecture

The system uses a **layered architecture** with clear separation of concerns:

### Core Components

- **`LibraryService`** - Facade coordinating all operations
- **`InventoryManager`** - Manages book catalog and availability
- **`PatronManager`** - Handles patron registration and information
- **`LoanManager`** - Controls checkout/return lifecycle
- **`ReservationManager`** - Manages book reservations and notifications

### Design Patterns Implemented

1. **Facade Pattern**: `LibraryService` provides simplified interface
2. **Observer Pattern**: Book availability notifications via `BookObserver`
3. **Strategy Pattern**: Pluggable recommendation algorithms via `RecommendationStrategy`
4. **Factory Pattern**: Object creation through `LibraryFactory`

## ✨ Features

### Core Features
- ✅ Book management (add, remove, update, search by title/author/ISBN)
- ✅ Patron management with borrowing history tracking
- ✅ Book checkout and return
- ✅ Real-time inventory tracking

### Advanced Features
- ✅ **Reservation System**: Queue for unavailable books with automatic notifications
- ✅ **Recommendation Engine**: Personalized book recommendations based on borrowing history
- ✅ **Event-driven Notifications**: Observer pattern for book availability alerts
- ✅ **Professional Logging**: SLF4J with Logback for comprehensive logging

## 🎯 SOLID Principles

See [SOLID_PRINCIPLES.md](SOLID_PRINCIPLES.md) for detailed explanation of:
- **S**ingle Responsibility Principle
- **O**pen/Closed Principle
- **L**iskov Substitution Principle
- **I**nterface Segregation Principle
- **D**ependency Inversion Principle

## 🚀 Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6+

### Running the Application

```bash
# Build the project
mvn clean compile

# Run the demo
mvn exec:java -Dexec.mainClass="org.lms.Main"
```

### Project Structure

```
src/main/java/org/lms/
├── Main.java                      # Application entry point
├── entity/
│   ├── Book.java                  # Book entity
│   ├── Patron.java                # Patron entity
│   └── Loan.java                  # Loan record entity
├── service/
│   ├── LibraryService.java        # Facade coordinator
│   ├── InventoryManager.java      # Book inventory management
│   ├── PatronManager.java         # Patron management
│   └── LoanManager.java           # Loan operations
├── factory/
│   └── LibraryFactory.java        # Factory for entity creation
├── observer/
│   ├── BookObserver.java          # Observer interface
│   └── ReservationManager.java    # Notification manager
└── recommendation/
    ├── RecommendationStrategy.java          # Strategy interface
    └── BasicRecommendationStrategy.java     # Recommendation implementation
```

## 📊 Logging

Logs are written to:
- **Console**: Real-time output with timestamps
- **File**: `logs/library-management.log` (30-day rolling retention)

Configuration: `src/main/resources/logback.xml`

## 🧪 Testing

(Unit tests to be added)

```bash
mvn test
```

## 📝 License

This project is created for educational purposes as part of the Airtribe Java course.

## 🎓 Learning Outcomes

This project demonstrates:
- Object-Oriented Programming (OOP) mastery
- SOLID principles in practice
- Design pattern implementation
- Clean architecture and code organization
- Professional logging practices
- Java best practices and idioms