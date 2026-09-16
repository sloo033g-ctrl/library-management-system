package model;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * Represents a single issue/return transaction record.
 * Demonstrates: Encapsulation.
 */
public class Transaction implements Serializable {
    private static final long serialVersionUID = 1L;

    public enum Status { ISSUED, RETURNED }

    private String transactionId;
    private String memberId;
    private String isbn;
    private LocalDate issueDate;
    private LocalDate dueDate;
    private LocalDate returnDate;
    private Status status;
    private double fineCharged;

    public Transaction(String transactionId, String memberId, String isbn,
                        LocalDate issueDate, LocalDate dueDate) {
        this.transactionId = transactionId;
        this.memberId = memberId;
        this.isbn = isbn;
        this.issueDate = issueDate;
        this.dueDate = dueDate;
        this.status = Status.ISSUED;
        this.fineCharged = 0.0;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public String getMemberId() {
        return memberId;
    }

    public String getIsbn() {
        return isbn;
    }

    public LocalDate getIssueDate() {
        return issueDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public LocalDate getReturnDate() {
        return returnDate;
    }

    public Status getStatus() {
        return status;
    }

    public double getFineCharged() {
        return fineCharged;
    }

    public void markReturned(LocalDate returnDate, double fineCharged) {
        this.returnDate = returnDate;
        this.fineCharged = fineCharged;
        this.status = Status.RETURNED;
    }

    @Override
    public String toString() {
        return String.format("TXN[%s] member=%s isbn=%s issued=%s due=%s returned=%s status=%s fine=%.2f",
                transactionId, memberId, isbn, issueDate, dueDate,
                returnDate == null ? "-" : returnDate, status, fineCharged);
    }
}
