package org.lms.entity;

import java.time.LocalDate;
import java.util.Objects;

public class Loan {
    private final String isbn;
    private final String patronId;
    private final LocalDate loanDate;
    private LocalDate returnDate;

    public Loan(String isbn, String patronId, LocalDate loanDate) {
        this.isbn = Objects.requireNonNull(isbn, "ISBN cannot be null");
        this.patronId = Objects.requireNonNull(patronId, "Patron ID cannot be null");
        this.loanDate = loanDate;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getPatronId() {
        return patronId;
    }

    public LocalDate getLoanDate() {
        return loanDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public void markReturned() {
        this.returnDate = LocalDate.now();
    }

    public String toString() {
        return String.format("Loan[ISBN=%s, PatronID=%s, LoanDate=%s, ReturnDate=%s]",
            isbn, patronId, loanDate, returnDate);
    }


}
