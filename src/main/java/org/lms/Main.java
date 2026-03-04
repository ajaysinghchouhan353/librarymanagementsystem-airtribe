package org.lms;

import org.lms.entity.Book;
import org.lms.entity.Patron;
import org.lms.factory.LibraryFactory;
import org.lms.observer.BookObserver;
import org.lms.recommendation.BasicRecommendationStrategy;
import org.lms.service.LibraryService;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {
    public static void main(String[] args) {
        Logger logger = Logger.getLogger("");
        logger.setLevel(Level.INFO);;

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
                logger.info("P2 notified: Book is now available: " + book);
                boolean checkedOut = libraryService.checkoutBook(book.getIsbn(), p2.getId());
                if(checkedOut) {
                    logger.info("P2 successfully checked out the book: " + book);
                } else {
                    logger.info("P2 failed to checkout the book: " + book);
                }
            }
        });

        //Return Book and trigger Notification
        libraryService.returnBook(b1.getIsbn());
        // Recommendations for p1 (simple): based on borrow history
        logger.info("Recommendations for p1: " + libraryService.recommendedBooks(p1.getId()));

        logger.info("Demo complete.");



    }
}