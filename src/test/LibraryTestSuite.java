package test;

import exception.BookNotAvailableException;
import exception.BookNotFoundException;
import model.Book;
import model.Member;
import service.LibraryService;
import service.TransactionService;
import util.PasswordUtil;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight, dependency-free validation test suite.
 * Run with: java -cp bin test.LibraryTestSuite
 *
 * (No external testing framework like JUnit is used, to keep the project
 * runnable with only a plain JDK. Each test prints PASS/FAIL and the suite
 * exits with a non-zero code if any assertion fails.)
 */
public class LibraryTestSuite {

    private static int passed = 0;
    private static int failed = 0;

    public static void main(String[] args) {
        testPasswordHashingIsConsistentAndVerifiable();
        testPasswordHashDiffersFromPlainText();
        testBookAvailabilityTracking();
        testIssueBookReducesAvailability();
        testIssuingUnavailableBookThrows();
        testFineCalculationForOnTimeReturn();
        testFineCalculationForLateReturn();
        testSingletonReturnsSameInstance();

        System.out.println("\n==============================");
        System.out.println("Passed: " + passed + " | Failed: " + failed);
        System.out.println("==============================");
        if (failed > 0) {
            System.exit(1);
        }
    }

    private static void testPasswordHashingIsConsistentAndVerifiable() {
        String hash1 = PasswordUtil.hash("mypassword");
        String hash2 = PasswordUtil.hash("mypassword");
        assertTrue("Hashing the same input twice yields the same hash",
                hash1.equals(hash2));
        assertTrue("verify() succeeds for the correct password",
                PasswordUtil.verify("mypassword", hash1));
        assertTrue("verify() fails for the wrong password",
                !PasswordUtil.verify("wrongpassword", hash1));
    }

    private static void testPasswordHashDiffersFromPlainText() {
        String plain = "admin123";
        String hash = PasswordUtil.hash(plain);
        assertTrue("Hash must not equal the plaintext password",
                !hash.equals(plain));
    }

    private static void testBookAvailabilityTracking() {
        Book book = new Book("T001", "Test Book", "Test Author", "Fiction", 2);
        assertTrue("New book with 2 copies should be available", book.isAvailable());
        book.decrementAvailable();
        book.decrementAvailable();
        assertTrue("Book with 0 available copies should be unavailable", !book.isAvailable());
        book.incrementAvailable();
        assertTrue("Book should become available again after increment", book.isAvailable());
    }

    private static void testIssueBookReducesAvailability() {
        List<Book> books = new ArrayList<>();
        Book book = new Book("T002", "Another Book", "Author X", "Non-fiction", 1);
        books.add(book);
        LibraryService.getInstance(books); // may already be initialized in earlier tests; fine either way
        LibraryService lib;
        try {
            lib = LibraryService.getInstance();
        } catch (IllegalStateException e) {
            lib = LibraryService.getInstance(books);
        }
        lib.addBook(book);

        TransactionService ts = new TransactionService(lib, new ArrayList<>());
        Member member = new Member("M001", "Test Member", "testmember", PasswordUtil.hash("pass"));

        try {
            ts.issueBook(member, "T002");
            assertTrue("Book should be unavailable after issuing the last copy",
                    !lib.getBookByIsbn("T002").isAvailable());
        } catch (BookNotFoundException | BookNotAvailableException e) {
            fail("Issuing an available book should not throw: " + e.getMessage());
        }
    }

    private static void testIssuingUnavailableBookThrows() {
        List<Book> books = new ArrayList<>();
        Book book = new Book("T003", "Zero Copy Book", "Author Y", "Fiction", 1);
        books.add(book);
        LibraryService lib;
        try {
            lib = LibraryService.getInstance();
        } catch (IllegalStateException e) {
            lib = LibraryService.getInstance(books);
        }
        lib.addBook(book);
        book.decrementAvailable(); // now 0 available

        TransactionService ts = new TransactionService(lib, new ArrayList<>());
        Member member = new Member("M002", "Another Member", "anothermember", PasswordUtil.hash("pass"));

        boolean threw = false;
        try {
            ts.issueBook(member, "T003");
        } catch (BookNotAvailableException e) {
            threw = true;
        } catch (BookNotFoundException e) {
            fail("Unexpected BookNotFoundException");
        }
        assertTrue("Issuing a book with 0 available copies should throw BookNotAvailableException", threw);
    }

    private static void testFineCalculationForOnTimeReturn() {
        TransactionService ts = new TransactionService(getLib(), new ArrayList<>());
        LocalDate due = LocalDate.of(2026, 1, 15);
        LocalDate returned = LocalDate.of(2026, 1, 10); // early
        double fine = ts.calculateFine(due, returned);
        assertTrue("On-time/early return should have zero fine", fine == 0.0);
    }

    private static void testFineCalculationForLateReturn() {
        TransactionService ts = new TransactionService(getLib(), new ArrayList<>());
        LocalDate due = LocalDate.of(2026, 1, 15);
        LocalDate returned = LocalDate.of(2026, 1, 20); // 5 days late
        double fine = ts.calculateFine(due, returned);
        assertTrue("5 days late should charge 25.0 (5 * 5.0/day), got " + fine, fine == 25.0);
    }

    private static void testSingletonReturnsSameInstance() {
        LibraryService instanceA = getLib();
        LibraryService instanceB = getLib();
        assertTrue("LibraryService.getInstance() must always return the same object",
                instanceA == instanceB);
    }

    private static LibraryService getLib() {
        try {
            return LibraryService.getInstance();
        } catch (IllegalStateException e) {
            return LibraryService.getInstance(new ArrayList<>());
        }
    }

    private static void assertTrue(String description, boolean condition) {
        if (condition) {
            System.out.println("[PASS] " + description);
            passed++;
        } else {
            System.out.println("[FAIL] " + description);
            failed++;
        }
    }

    private static void fail(String message) {
        System.out.println("[FAIL] " + message);
        failed++;
    }
}
