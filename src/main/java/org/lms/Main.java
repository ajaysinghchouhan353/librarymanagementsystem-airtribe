package org.lms;

import org.lms.entity.Book;
import org.lms.entity.Branch;
import org.lms.entity.Patron;
import org.lms.factory.LibraryFactory;
import org.lms.observer.BookObserver;
import org.lms.recommendation.BasicRecommendationStrategy;
import org.lms.service.LibraryService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        LibraryService libraryService = new LibraryService();
        libraryService.setRecommendationStrategy(new BasicRecommendationStrategy());

        // create books and patrons via factory
        Book b1 = LibraryFactory.createBook("978-0134685991", "Effective Java", "Joshua Bloch", 2018);
        Book b2 = LibraryFactory.createBook("978-0596009205", "Head First Java", "Kathy Sierra", 2005);
        Book b3 = LibraryFactory.createBook("978-1617294945", "Java Concurrency in Practice", "Brian Goetz", 2006);

        libraryService.addBook(b1);
        libraryService.addBook(b2);
        libraryService.addBook(b3);

        Patron p1 = LibraryFactory.createPatron("p1", "Alice", "alice@example.com");
        Patron p2 = LibraryFactory.createPatron("p2", "Bob", "bob@example.com");

        libraryService.addPatron(p1);
        libraryService.addPatron(p2);

        //Checkout and Return Books Flow
        libraryService.checkoutBook(b1.getIsbn(), p1.getId());
        libraryService.checkoutBook(b2.getIsbn(), p1.getId());

        //P2 tries to checkout a book that is already checked out
        boolean ok = libraryService.checkoutBook(b1.getIsbn(), p2.getId());
        if(!ok) {
            logger.info("Book is currently unavailable, reserving for P2");
        }

        // Demonstrate reservation/observer: p2 reserves b1 and will be notified when it's available
        libraryService.reserveBook(b1.getIsbn(), new BookObserver() {
            @Override
            public void onBookAvailable(Book book) {
                logger.info("P2 notified: Book is now available: {}", book);
                boolean checkedOut = libraryService.checkoutBook(book.getIsbn(), p2.getId());
                if(checkedOut) {
                    logger.info("P2 successfully checked out the book: {}", book);
                } else {
                    logger.info("P2 failed to checkout the book: {}", book);
                }
            }
        });

        //Return Book and trigger Notification
        libraryService.returnBook(b1.getIsbn());
        // Recommendations for p1 (simple): based on borrow history
        logger.info("Recommendations for p1: {}", libraryService.recommendedBooks(p1.getId()));

        logger.info("=== Basic Demo complete. ===\n");

        // ========== Multi-Branch Support Demo ==========
        logger.info("=== Multi-Branch Support Demo ===");

        // Create branches
        Branch downtown = LibraryFactory.createBranch("BRANCH-001", "Downtown Branch", 
                "123 Main St, Downtown", "555-0101");
        Branch uptown = LibraryFactory.createBranch("BRANCH-002", "Uptown Branch", 
                "456 High St, Uptown", "555-0102");
        Branch westside = LibraryFactory.createBranch("BRANCH-003", "Westside Branch", 
                "789 West Ave, Westside", "555-0103");

        libraryService.addBranch(downtown);
        libraryService.addBranch(uptown);
        libraryService.addBranch(westside);

        logger.info("Total branches: {}", libraryService.getAllBranches().size());

        // Add book copies to branches
        libraryService.addBookToBranch("BRANCH-001", b1.getIsbn(), 5); // 5 copies at Downtown
        libraryService.addBookToBranch("BRANCH-001", b2.getIsbn(), 3); // 3 copies at Downtown
        libraryService.addBookToBranch("BRANCH-002", b1.getIsbn(), 2); // 2 copies at Uptown
        libraryService.addBookToBranch("BRANCH-002", b3.getIsbn(), 4); // 4 copies at Uptown
        libraryService.addBookToBranch("BRANCH-003", b2.getIsbn(), 6); // 6 copies at Westside

        // Check availability at branches
        logger.info("Effective Java copies at Downtown: {}", 
                libraryService.getBookCountAtBranch("BRANCH-001", b1.getIsbn()));
        logger.info("Effective Java copies at Uptown: {}", 
                libraryService.getBookCountAtBranch("BRANCH-002", b1.getIsbn()));
        logger.info("Effective Java total across all branches: {}", 
                libraryService.getTotalCopiesAcrossAllBranches(b1.getIsbn()));

        // Find branches with a specific book
        logger.info("Branches with 'Head First Java': {}", 
                libraryService.findBranchesWithBook(b2.getIsbn()));

        // Transfer book between branches
        logger.info("Transferring 'Effective Java' from Downtown to Westside...");
        libraryService.transferBook(b1.getIsbn(), "BRANCH-001", "BRANCH-003")
                .ifPresentOrElse(
                    transferId -> logger.info("Transfer successful! Transfer ID: {}", transferId),
                    () -> logger.warn("Transfer failed!")
                );

        // Check updated counts after transfer
        logger.info("After transfer - Downtown has {} copies, Westside has {} copies",
                libraryService.getBookCountAtBranch("BRANCH-001", b1.getIsbn()),
                libraryService.getBookCountAtBranch("BRANCH-003", b1.getIsbn()));

        // View all transfers
        logger.info("Total transfers: {}", libraryService.getAllTransfers().size());

        logger.info("=== Multi-Branch Demo complete! ===");


    }
}