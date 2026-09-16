package service;

import exception.BookNotFoundException;
import interfaces.Searchable;
import model.Book;
import util.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Singleton Design Pattern: only one LibraryService instance manages
 * the book catalog for the entire application.
 * Module: Book / Inventory Management.
 * Implements Searchable interface (Abstraction / Polymorphism).
 */
public class LibraryService implements Searchable {

    private static LibraryService instance;
    private Map<String, Book> catalog; // keyed by ISBN for O(1) lookup (Performance NFR)

    private LibraryService(List<Book> initialBooks) {
        catalog = new HashMap<>();
        if (initialBooks != null) {
            for (Book b : initialBooks) {
                catalog.put(b.getIsbn(), b);
            }
        }
    }

    public static synchronized LibraryService getInstance(List<Book> initialBooks) {
        if (instance == null) {
            instance = new LibraryService(initialBooks);
        }
        return instance;
    }

    public static LibraryService getInstance() {
        if (instance == null) {
            throw new IllegalStateException("LibraryService not initialized yet.");
        }
        return instance;
    }

    public void addBook(Book book) {
        catalog.put(book.getIsbn(), book);
        Logger.info("Book added: " + book.getIsbn() + " - " + book.getTitle());
    }

    public void updateBook(String isbn, String title, String author, String genre) throws BookNotFoundException {
        Book book = getBookByIsbn(isbn);
        book.setTitle(title);
        book.setAuthor(author);
        book.setGenre(genre);
        Logger.info("Book updated: " + isbn);
    }

    public void deleteBook(String isbn) throws BookNotFoundException {
        if (!catalog.containsKey(isbn)) {
            throw new BookNotFoundException("No book found with ISBN: " + isbn);
        }
        catalog.remove(isbn);
        Logger.info("Book deleted: " + isbn);
    }

    public Book getBookByIsbn(String isbn) throws BookNotFoundException {
        Book book = catalog.get(isbn);
        if (book == null) {
            throw new BookNotFoundException("No book found with ISBN: " + isbn);
        }
        return book;
    }

    public List<Book> getAllBooks() {
        return new ArrayList<>(catalog.values());
    }

    @Override
    public List<Book> searchByTitle(String title) {
        List<Book> results = new ArrayList<>();
        for (Book b : catalog.values()) {
            if (b.getTitle().toLowerCase().contains(title.toLowerCase())) {
                results.add(b);
            }
        }
        return results;
    }

    @Override
    public List<Book> searchByAuthor(String author) {
        List<Book> results = new ArrayList<>();
        for (Book b : catalog.values()) {
            if (b.getAuthor().toLowerCase().contains(author.toLowerCase())) {
                results.add(b);
            }
        }
        return results;
    }

    @Override
    public List<Book> searchByGenre(String genre) {
        List<Book> results = new ArrayList<>();
        for (Book b : catalog.values()) {
            if (b.getGenre().equalsIgnoreCase(genre)) {
                results.add(b);
            }
        }
        return results;
    }
}
