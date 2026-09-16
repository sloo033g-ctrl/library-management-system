package interfaces;

import model.Book;
import java.util.List;

/**
 * Contract for any service that supports searching books.
 */
public interface Searchable {
    List<Book> searchByTitle(String title);
    List<Book> searchByAuthor(String author);
    List<Book> searchByGenre(String genre);
}
