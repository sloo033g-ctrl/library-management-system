package service;

import exception.BookNotAvailableException;
import exception.BookNotFoundException;
import interfaces.Renewable;
import model.Book;
import model.Member;
import model.Transaction;
import util.IdGenerator;
import util.Logger;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles issuing, returning, and fine calculation.
 * Module: Transaction Management.
 * Implements Renewable interface (Abstraction / Polymorphism).
 */
public class TransactionService implements Renewable {

    private static final int LOAN_PERIOD_DAYS = 14;
    private static final double FINE_PER_DAY = 5.0; // currency units per day overdue

    private Map<String, Transaction> transactions; // keyed by transactionId
    private LibraryService libraryService;

    public TransactionService(LibraryService libraryService, List<Transaction> initialTransactions) {
        this.libraryService = libraryService;
        this.transactions = new HashMap<>();
        if (initialTransactions != null) {
            for (Transaction t : initialTransactions) {
                transactions.put(t.getTransactionId(), t);
            }
        }
    }

    public Transaction issueBook(Member member, String isbn)
            throws BookNotFoundException, BookNotAvailableException {
        Book book = libraryService.getBookByIsbn(isbn);
        if (!book.isAvailable()) {
            throw new BookNotAvailableException("Book is currently unavailable: " + book.getTitle());
        }
        book.decrementAvailable();
        member.addBorrowedBook(isbn);

        String txnId = IdGenerator.nextTransactionId();
        LocalDate issueDate = LocalDate.now();
        LocalDate dueDate = issueDate.plusDays(LOAN_PERIOD_DAYS);
        Transaction txn = new Transaction(txnId, member.getUserId(), isbn, issueDate, dueDate);
        transactions.put(txnId, txn);

        Logger.info("Book issued: " + isbn + " to member " + member.getUserId());
        return txn;
    }

    public double returnBook(Member member, String transactionId) throws BookNotFoundException {
        Transaction txn = transactions.get(transactionId);
        if (txn == null) {
            throw new IllegalArgumentException("No such transaction: " + transactionId);
        }
        Book book = libraryService.getBookByIsbn(txn.getIsbn());
        LocalDate today = LocalDate.now();

        double fine = calculateFine(txn.getDueDate(), today);
        txn.markReturned(today, fine);
        book.incrementAvailable();
        member.removeBorrowedBook(txn.getIsbn());

        if (fine > 0) {
            member.addFine(fine);
        }

        Logger.info("Book returned: " + txn.getIsbn() + " by member " + member.getUserId()
                + " fine=" + fine);
        return fine;
    }

    public double calculateFine(LocalDate dueDate, LocalDate returnDate) {
        long overdueDays = ChronoUnit.DAYS.between(dueDate, returnDate);
        return overdueDays > 0 ? overdueDays * FINE_PER_DAY : 0.0;
    }

    @Override
    public boolean renew(String transactionId, int extraDays) {
        Transaction txn = transactions.get(transactionId);
        if (txn == null || txn.getStatus() != Transaction.Status.ISSUED) {
            return false;
        }
        // Rebuild with extended due date since fields are private/final-like in usage
        Transaction renewed = new Transaction(txn.getTransactionId(), txn.getMemberId(),
                txn.getIsbn(), txn.getIssueDate(), txn.getDueDate().plusDays(extraDays));
        transactions.put(transactionId, renewed);
        Logger.info("Transaction renewed: " + transactionId + " by " + extraDays + " days");
        return true;
    }

    public List<Transaction> getTransactionsForMember(String memberId) {
        List<Transaction> result = new ArrayList<>();
        for (Transaction t : transactions.values()) {
            if (t.getMemberId().equals(memberId)) {
                result.add(t);
            }
        }
        return result;
    }

    public List<Transaction> getAllTransactions() {
        return new ArrayList<>(transactions.values());
    }

    public List<Transaction> getOverdueTransactions() {
        List<Transaction> result = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (Transaction t : transactions.values()) {
            if (t.getStatus() == Transaction.Status.ISSUED && t.getDueDate().isBefore(today)) {
                result.add(t);
            }
        }
        return result;
    }
}
