package org.lms.entity;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class Patron {
    private final String id;
    private String name;
    private String email;
    private final List<String> borrowedBooksISBN = new ArrayList<>();

    public Patron(String id, String name, String email) {
        this.id = Objects.requireNonNull(id, "ID cannot be null");
        this.name = Objects.requireNonNull(name, "Name cannot be null");
        this.email = Objects.requireNonNull(email, "Email cannot be null");
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void recordBorrowedBook(String isbn) {
        borrowedBooksISBN.add(isbn);
    }

    public List<String> getBorrowedBooksHistory() {
        return Collections.unmodifiableList(borrowedBooksISBN);
    }

    @Override
    public String toString() {
       return String.format("Patron{id='%s', name='%s', email='%s', borrowedBooksISBN=%s}", id, name, email, borrowedBooksISBN);
    }

}
