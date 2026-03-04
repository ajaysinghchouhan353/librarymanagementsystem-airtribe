package org.lms.factory;

import org.lms.entity.Book;
import org.lms.entity.Patron;

public final class LibraryFactory {
    private LibraryFactory() {}

    public static Book createBook(String isbn, String title, String author, int publicationYear) {
        return new Book(isbn, title, author, publicationYear);
    }

    public static Patron createPatron(String id, String name, String email) {
        return new Patron(id, name, email);
    }
}
