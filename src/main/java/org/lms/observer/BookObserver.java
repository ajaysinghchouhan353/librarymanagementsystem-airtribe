package org.lms.observer;

import org.lms.entity.Book;

public interface BookObserver {
    void onBookAvailable(Book book);
}
