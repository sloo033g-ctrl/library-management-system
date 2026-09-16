package model;

import java.util.ArrayList;
import java.util.List;

/**
 * Member - a regular library user who can borrow books.
 * Demonstrates: Inheritance, Polymorphism, Encapsulation.
 */
public class Member extends User {
    private static final long serialVersionUID = 1L;

    private List<String> borrowedBookIsbns;
    private double outstandingFine;

    public Member(String userId, String name, String username, String passwordHash) {
        super(userId, name, username, passwordHash);
        this.borrowedBookIsbns = new ArrayList<>();
        this.outstandingFine = 0.0;
    }

    @Override
    public String getRole() {
        return "MEMBER";
    }

    @Override
    public String[] getPermissions() {
        return new String[] { "SEARCH_BOOK", "BORROW_BOOK", "RETURN_BOOK", "VIEW_HISTORY" };
    }

    public List<String> getBorrowedBookIsbns() {
        return borrowedBookIsbns;
    }

    public void addBorrowedBook(String isbn) {
        borrowedBookIsbns.add(isbn);
    }

    public void removeBorrowedBook(String isbn) {
        borrowedBookIsbns.remove(isbn);
    }

    public double getOutstandingFine() {
        return outstandingFine;
    }

    public void addFine(double amount) {
        this.outstandingFine += amount;
    }

    public void clearFine() {
        this.outstandingFine = 0.0;
    }
}
