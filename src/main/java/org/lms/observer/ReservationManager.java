package org.lms.observer;

import org.lms.entity.Book;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ReservationManager {
    private static final Logger log = Logger.getLogger(ReservationManager.class.getName());

    //isbn -> queue of observers (patron ids could be represented as observers)
    private final Map<String, Deque<BookObserver>> reservationMap = new HashMap<>();

    public synchronized void reserveBook(String isbn, BookObserver observer) {
        reservationMap.computeIfAbsent(isbn, k -> new ArrayDeque<>()).add(observer);
        log.info(() -> "Book reserved: ISBN=" + isbn + " by observer=" + observer);
    }

    public synchronized boolean hasReservation(String isbn) {
        Deque<BookObserver> observers = reservationMap.get(isbn);
        return observers != null && !observers.isEmpty();
    }

    public synchronized void notifyBookAvailable(Book book) {
        Deque<BookObserver> observers = reservationMap.get(book.getIsbn());
        if(observers == null || observers.isEmpty()) {
            log.info(() -> "No reservations for book: " + book);
            return;
        }
        BookObserver observer = observers.poll(); // Get the next observer in line
        if(observer != null) {
            log.info(() -> "Notifying observer: " + observer + " about availability of book: " + book);
            observer.onBookAvailable(book);
        }
    }
}
