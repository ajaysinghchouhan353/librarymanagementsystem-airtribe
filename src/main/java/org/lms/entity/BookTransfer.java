package org.lms.entity;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Represents a book transfer between branches.
 */
public class BookTransfer {
    private final String transferId;
    private final String isbn;
    private final String fromBranchId;
    private final String toBranchId;
    private final LocalDateTime requestedAt;
    private LocalDateTime completedAt;
    private TransferStatus status;

    public enum TransferStatus {
        PENDING,
        IN_TRANSIT,
        COMPLETED,
        CANCELLED
    }

    public BookTransfer(String transferId, String isbn, String fromBranchId, String toBranchId) {
        this.transferId = Objects.requireNonNull(transferId, "Transfer ID cannot be null");
        this.isbn = Objects.requireNonNull(isbn, "ISBN cannot be null");
        this.fromBranchId = Objects.requireNonNull(fromBranchId, "From branch ID cannot be null");
        this.toBranchId = Objects.requireNonNull(toBranchId, "To branch ID cannot be null");
        this.requestedAt = LocalDateTime.now();
        this.status = TransferStatus.PENDING;
    }

    public String getTransferId() {
        return transferId;
    }

    public String getIsbn() {
        return isbn;
    }

    public String getFromBranchId() {
        return fromBranchId;
    }

    public String getToBranchId() {
        return toBranchId;
    }

    public LocalDateTime getRequestedAt() {
        return requestedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public TransferStatus getStatus() {
        return status;
    }

    public void setStatus(TransferStatus status) {
        this.status = status;
        if (status == TransferStatus.COMPLETED) {
            this.completedAt = LocalDateTime.now();
        }
    }

    @Override
    public String toString() {
        return String.format("BookTransfer{id='%s', isbn='%s', from='%s', to='%s', status=%s}",
                transferId, isbn, fromBranchId, toBranchId, status);
    }
}
