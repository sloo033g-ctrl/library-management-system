import exception.BookNotAvailableException;
import exception.BookNotFoundException;
import exception.InvalidCredentialsException;
import model.Book;
import model.Librarian;
import model.Member;
import model.Transaction;
import model.User;
import service.AuthService;
import service.LibraryService;
import service.ReportService;
import service.TransactionService;
import util.FileManager;
import util.Logger;

import java.util.List;
import java.util.Scanner;

/**
 * Entry point of the Library Management System.
 * Provides a console-based menu driven workflow for Librarians and Members.
 */
public class Main {

    private static final String USERS_FILE = "data/users.dat";
    private static final String BOOKS_FILE = "data/books.dat";
    private static final String TXNS_FILE = "data/transactions.dat";

    private static Scanner scanner = new Scanner(System.in);
    private static AuthService authService;
    private static LibraryService libraryService;
    private static TransactionService transactionService;
    private static ReportService reportService;

    public static void main(String[] args) {
        bootstrap();
        Logger.info("Application started.");

        System.out.println("=================================================");
        System.out.println(" Welcome to the Library Management System");
        System.out.println("=================================================");

        boolean running = true;
        while (running) {
            System.out.println("\n1. Login\n2. Register\n3. Exit");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    handleLogin();
                    break;
                case "2":
                    handleRegister();
                    break;
                case "3":
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }

        persist();
        Logger.info("Application shut down gracefully.");
        System.out.println("Goodbye!");
    }

    private static void bootstrap() {
        List<User> users = FileManager.load(USERS_FILE);
        List<Book> books = FileManager.load(BOOKS_FILE);
        List<Transaction> transactions = FileManager.load(TXNS_FILE);

        authService = new AuthService(users);
        libraryService = LibraryService.getInstance(books);
        transactionService = new TransactionService(libraryService, transactions);
        reportService = new ReportService(libraryService, transactionService);

        // Seed a default librarian account and a couple of books on first run.
        if (users == null) {
            authService.register("LIBRARIAN", "Default Admin", "admin", "admin123");
        }
        if (books == null) {
            libraryService.addBook(new Book("ISBN001", "Clean Code", "Robert C. Martin", "Software", 3));
            libraryService.addBook(new Book("ISBN002", "Effective Java", "Joshua Bloch", "Software", 2));
            libraryService.addBook(new Book("ISBN003", "The Pragmatic Programmer", "Andy Hunt", "Software", 1));
        }
    }

    private static void persist() {
        FileManager.save(USERS_FILE, authService.getAllUsers());
        FileManager.save(BOOKS_FILE, libraryService.getAllBooks());
        FileManager.save(TXNS_FILE, transactionService.getAllTransactions());
    }

    private static void handleRegister() {
        System.out.print("Role (LIBRARIAN/MEMBER): ");
        String role = scanner.nextLine().trim();
        System.out.print("Full name: ");
        String name = scanner.nextLine().trim();
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.register(role, name, username, password);
            System.out.println("Registered successfully: " + user);
        } catch (IllegalArgumentException e) {
            System.out.println("Registration failed: " + e.getMessage());
        }
    }

    private static void handleLogin() {
        System.out.print("Username: ");
        String username = scanner.nextLine().trim();
        System.out.print("Password: ");
        String password = scanner.nextLine().trim();

        try {
            User user = authService.login(username, password);
            System.out.println("Welcome, " + user.getName() + " (" + user.getRole() + ")");

            if (user instanceof Librarian) {
                librarianMenu((Librarian) user);
            } else if (user instanceof Member) {
                memberMenu((Member) user);
            }
        } catch (InvalidCredentialsException e) {
            System.out.println("Login failed: " + e.getMessage());
        }
    }

    private static void librarianMenu(Librarian librarian) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Librarian Menu (" + librarian.getName() + ") ---");
            System.out.println("1. Add Book");
            System.out.println("2. Update Book");
            System.out.println("3. Delete Book");
            System.out.println("4. View All Books");
            System.out.println("5. Inventory Report");
            System.out.println("6. Overdue Report");
            System.out.println("7. All Transactions Report");
            System.out.println("8. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        addBookFlow();
                        break;
                    case "2":
                        updateBookFlow();
                        break;
                    case "3":
                        deleteBookFlow();
                        break;
                    case "4":
                        printAllBooks();
                        break;
                    case "5":
                        reportService.printInventorySummary();
                        break;
                    case "6":
                        reportService.printOverdueReport();
                        break;
                    case "7":
                        reportService.printAllTransactions();
                        break;
                    case "8":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                Logger.error(e.getMessage());
            }
        }
    }

    private static void memberMenu(Member member) {
        boolean back = false;
        while (!back) {
            System.out.println("\n--- Member Menu (" + member.getName() + ") ---");
            System.out.println("1. Search Books");
            System.out.println("2. Borrow Book");
            System.out.println("3. Return Book");
            System.out.println("4. View My Borrowed Books");
            System.out.println("5. View My Outstanding Fine");
            System.out.println("6. Logout");
            System.out.print("Choose an option: ");
            String choice = scanner.nextLine().trim();

            try {
                switch (choice) {
                    case "1":
                        searchBooksFlow();
                        break;
                    case "2":
                        borrowBookFlow(member);
                        break;
                    case "3":
                        returnBookFlow(member);
                        break;
                    case "4":
                        System.out.println("Borrowed ISBNs: " + member.getBorrowedBookIsbns());
                        break;
                    case "5":
                        System.out.printf("Outstanding fine: %.2f%n", member.getOutstandingFine());
                        break;
                    case "6":
                        back = true;
                        break;
                    default:
                        System.out.println("Invalid option.");
                }
            } catch (Exception e) {
                System.out.println("Error: " + e.getMessage());
                Logger.error(e.getMessage());
            }
        }
    }

    private static void addBookFlow() {
        System.out.print("ISBN: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("Title: ");
        String title = scanner.nextLine().trim();
        System.out.print("Author: ");
        String author = scanner.nextLine().trim();
        System.out.print("Genre: ");
        String genre = scanner.nextLine().trim();
        System.out.print("Total copies: ");
        int copies = Integer.parseInt(scanner.nextLine().trim());

        libraryService.addBook(new Book(isbn, title, author, genre, copies));
        System.out.println("Book added successfully.");
    }

    private static void updateBookFlow() throws BookNotFoundException {
        System.out.print("ISBN of book to update: ");
        String isbn = scanner.nextLine().trim();
        System.out.print("New title: ");
        String title = scanner.nextLine().trim();
        System.out.print("New author: ");
        String author = scanner.nextLine().trim();
        System.out.print("New genre: ");
        String genre = scanner.nextLine().trim();

        libraryService.updateBook(isbn, title, author, genre);
        System.out.println("Book updated successfully.");
    }

    private static void deleteBookFlow() throws BookNotFoundException {
        System.out.print("ISBN of book to delete: ");
        String isbn = scanner.nextLine().trim();
        libraryService.deleteBook(isbn);
        System.out.println("Book deleted successfully.");
    }

    private static void printAllBooks() {
        List<Book> books = libraryService.getAllBooks();
        System.out.println("---- All Books (" + books.size() + ") ----");
        for (Book b : books) {
            System.out.println(b);
        }
    }

    private static void searchBooksFlow() {
        System.out.println("Search by: 1. Title  2. Author  3. Genre");
        String opt = scanner.nextLine().trim();
        System.out.print("Search term: ");
        String term = scanner.nextLine().trim();

        List<Book> results;
        switch (opt) {
            case "1":
                results = libraryService.searchByTitle(term);
                break;
            case "2":
                results = libraryService.searchByAuthor(term);
                break;
            case "3":
                results = libraryService.searchByGenre(term);
                break;
            default:
                System.out.println("Invalid option.");
                return;
        }

        System.out.println("---- Results (" + results.size() + ") ----");
        for (Book b : results) {
            System.out.println(b);
        }
    }

    private static void borrowBookFlow(Member member) throws BookNotFoundException, BookNotAvailableException {
        System.out.print("ISBN to borrow: ");
        String isbn = scanner.nextLine().trim();
        Transaction txn = transactionService.issueBook(member, isbn);
        System.out.println("Book issued. Transaction ID: " + txn.getTransactionId()
                + " | Due date: " + txn.getDueDate());
    }

    private static void returnBookFlow(Member member) throws BookNotFoundException {
        System.out.print("Transaction ID: ");
        String txnId = scanner.nextLine().trim();
        double fine = transactionService.returnBook(member, txnId);
        if (fine > 0) {
            System.out.printf("Book returned late. Fine charged: %.2f%n", fine);
        } else {
            System.out.println("Book returned on time. No fine.");
        }
    }
}
