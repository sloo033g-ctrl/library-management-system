package service;

import model.Book;
import model.Transaction;

import java.util.List;

/**
 * Generates simple analytics/reports from library data.
 * Part of the Book Management / Transaction Management modules'
 * reporting responsibility.
 */
public class ReportService {

    private LibraryService libraryService;
    private TransactionService transactionService;

    public ReportService(LibraryService libraryService, TransactionService transactionService) {
        this.libraryService = libraryService;
        this.transactionService = transactionService;
    }

    public void printInventorySummary() {
        List<Book> books = libraryService.getAllBooks();
        int totalTitles = books.size();
        int totalCopies = 0;
        int availableCopies = 0;
        for (Book b : books) {
            totalCopies += b.getTotalCopies();
            availableCopies += b.getAvailableCopies();
        }
        System.out.println("---- Inventory Summary ----");
        System.out.println("Distinct titles   : " + totalTitles);
        System.out.println("Total copies      : " + totalCopies);
        System.out.println("Available copies  : " + availableCopies);
        System.out.println("Copies on loan    : " + (totalCopies - availableCopies));
    }

    public void printOverdueReport() {
        List<Transaction> overdue = transactionService.getOverdueTransactions();
        System.out.println("---- Overdue Transactions (" + overdue.size() + ") ----");
        for (Transaction t : overdue) {
            System.out.println(t);
        }
    }

    public void printAllTransactions() {
        List<Transaction> all = transactionService.getAllTransactions();
        System.out.println("---- All Transactions (" + all.size() + ") ----");
        for (Transaction t : all) {
            System.out.println(t);
        }
    }
}
