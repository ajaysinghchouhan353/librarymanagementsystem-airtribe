package org.lms.observer;

import org.lms.entity.Book;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;

public class ReservationManager {
    private static final Logger log = LoggerFactory.getLogger(ReservationManager.class);

    //isbn -> queue of observers (patron ids could be represented as observers)
    private final Map<String, Deque<BookObserver>> reservationMap = new HashMap<>();

    public synchronized void reserveBook(String isbn, BookObserver observer) {
        reservationMap.computeIfAbsent(isbn, k -> new ArrayDeque<>()).add(observer);
        log.info("Book reserved: ISBN={} by observer={}", isbn, observer);
    }

    public synchronized boolean hasReservation(String isbn) {
        Deque<BookObserver> observers = reservationMap.get(isbn);
        return observers != null && !observers.isEmpty();
    }

    public synchronized void notifyBookAvailable(Book book) {
        Deque<BookObserver> observers = reservationMap.get(book.getIsbn());
        if(observers == null || observers.isEmpty()) {
            log.info("No reservations for book: {}", book);
            return;
        }
        BookObserver observer = observers.poll(); // Get the next observer in line
        if(observer != null) {
            log.info("Notifying observer: {} about availability of book: {}", observer, book);
            observer.onBookAvailable(book);
        }
    }
}
