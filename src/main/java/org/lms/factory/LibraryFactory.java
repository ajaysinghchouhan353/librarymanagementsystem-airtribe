package org.lms.factory;

import org.lms.entity.Book;
import org.lms.entity.Branch;
import org.lms.entity.Patron;

public final class LibraryFactory {
    private LibraryFactory() {}

    public static Book createBook(String isbn, String title, String author, int publicationYear) {
        return new Book(isbn, title, author, publicationYear);
    }

    public static Patron createPatron(String id, String name, String email) {
        return new Patron(id, name, email);
    }

    public static Branch createBranch(String id, String name, String address) {
        return new Branch(id, name, address);
    }

    public static Branch createBranch(String id, String name, String address, String phoneNumber) {
        return new Branch(id, name, address, phoneNumber);
    }
}
