package org.lms.service;

import org.lms.entity.Book;
import org.lms.entity.Loan;
import org.lms.entity.Patron;
import org.lms.observer.BookObserver;
import org.lms.observer.ReservationManager;
import org.lms.recommendation.RecommendationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class LibraryService {
    private static final Logger log = LoggerFactory.getLogger(LibraryService.class);

    private final Map<String, Book> inventory = new HashMap<>(); //isbn -> Book
    private final Map<String, Patron> patrons = new HashMap<>(); //patronId -> Patron
    private final Map<String, Loan> activeLoans = new HashMap<>(); //isbn -> Loan
    private final List<Loan> loanHistory = new ArrayList<>();
    private final ReservationManager reservationManager = new ReservationManager();
    private RecommendationStrategy recommendationStrategy;

    public LibraryService() {}

    public void setRecommendationStrategy(RecommendationStrategy strategy) {
        this.recommendationStrategy = strategy;
    }

    //Book Management
   public synchronized void addBook(Book book) {
        inventory.put(book.getIsbn(), book);
        log.info("Book added: {}", book);
   }

   public synchronized  void removeBook(String isbn) {
        inventory.remove(isbn);
        log.info("Book removed: {}", isbn);
   }

   public synchronized  void updateBook(Book book) {
        inventory.put(book.getIsbn(), book);
        log.info("Book updated: {}", book);
   }

   public Optional<Book> findByIsbn(String isbn) {
        return Optional.ofNullable(inventory.get(isbn));
   }

   public List<Book> findByTitle(String title) {
        String lowerTitle = title.toLowerCase();
        return inventory.values().stream().filter(b->b.getTitle().toLowerCase().contains(lowerTitle)).collect(Collectors.toList());
   }

   public List<Book> findByAuthor(String author) {
        String lowerAuthor = author.toLowerCase();
        return inventory.values().stream().filter(b->b.getAuthor().toLowerCase().contains(lowerAuthor)).collect(Collectors.toList());
   }

   //Patron Management
    public synchronized void addPatron(Patron patron) {
        patrons.put(patron.getId(), patron);
        log.info("Patron added: {}", patron);
    }

    public synchronized void removePatron(Patron patron) {
        patrons.remove(patron.getId());
        log.info("Patron removed: {}", patron);
    }

    public synchronized void updatePatron(Patron patron) {
        patrons.put(patron.getId(), patron);
        log.info("Patron updated: {}", patron);
    }

    public Optional<Patron> findPatronById(String id) {
        return Optional.ofNullable(patrons.get(id));
    }

    //Lending Process
    public synchronized boolean checkoutBook(String isbn, String patronId) {
        if(!inventory.containsKey(isbn)) {
            log.warn("Checkout failed: Book not found - {}", isbn);
            return false;
        }
        if(activeLoans.containsKey(isbn)) {
            log.warn("Checkout failed: Book already loaned - {}", isbn);
            return false;
        }
        if(!patrons.containsKey(patronId)) {
            log.warn("Checkout failed: Patron not found - {}", patronId);
            return false;
        }
        Patron patron = patrons.get(patronId);
        Loan loan = new Loan(isbn, patronId, LocalDate.now());
        activeLoans.put(isbn, loan);
        loanHistory.add(loan);
        patron.recordBorrowedBook(isbn);
        log.info("Book checked out: {} to {}", isbn, patron.getName());
        return true;
    }

    public synchronized boolean returnBook(String isbn) {
        Loan loan = activeLoans.remove(isbn);
        if(loan == null) {
            log.warn("Return failed: Book not currently loaned - {}", isbn);
            return false;
        }
        loan.markReturned();
        log.info("Book returned: {}", isbn);
        reservationManager.notifyBookAvailable(inventory.get(isbn));
        return true;
    }

    public synchronized void reserveBook(String isbn, BookObserver observer) {
        if(!inventory.containsKey(isbn)) {
            log.warn("Reservation failed: Book not found - {}", isbn);
            return;
        }
        reservationManager.reserveBook(isbn, observer);
    }

    public List<Book> recommendedBooks(String patronId) {
        if(recommendationStrategy == null) {
            log.warn("No recommendation strategy set");
            return Collections.emptyList();
        }
        Patron patron = patrons.get(patronId);
        if(patron == null) {
            log.warn("Recommendation failed: Patron not found - {}", patronId);
            return Collections.emptyList();
        }
        return recommendationStrategy.recommendBooks(patronId, patron.getBorrowedBooksHistory(), getAllBooks());
    }

    public synchronized boolean isAvailable(String isbn) {
        return inventory.containsKey(isbn) && !activeLoans.containsKey(isbn);
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(inventory.values());
    }

    public List<Loan> getLoanHistory() {
        return Collections.unmodifiableList(loanHistory);
    }
}
