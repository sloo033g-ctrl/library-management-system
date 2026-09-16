package web;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import exception.BookNotAvailableException;
import exception.BookNotFoundException;
import exception.InvalidCredentialsException;
import model.Book;
import model.Member;
import model.Transaction;
import model.User;
import service.AuthService;
import service.LibraryService;
import service.ReportService;
import service.TransactionService;
import util.FileManager;
import util.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Embedded REST API server exposing the Library Management System's
 * existing services (AuthService, LibraryService, TransactionService,
 * ReportService) over HTTP as JSON, so a web frontend can drive the same
 * business logic used by the console app.
 *
 * Uses only classes bundled with the JDK (com.sun.net.httpserver) —
 * no external dependencies required.
 */
public class WebServer {

    private static final String USERS_FILE = "data/users.dat";
    private static final String BOOKS_FILE = "data/books.dat";
    private static final String TXNS_FILE = "data/transactions.dat";
    private static final int PORT = 8080;

    private static AuthService authService;
    private static LibraryService libraryService;
    private static TransactionService transactionService;
    private static ReportService reportService;

    public static void main(String[] args) throws IOException {
        bootstrap();

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/api/register", wrap(WebServer::handleRegister));
        server.createContext("/api/login", wrap(WebServer::handleLogin));
        server.createContext("/api/books/search", wrap(WebServer::handleSearchBooks));
        server.createContext("/api/books", wrap(WebServer::handleBooks));
        server.createContext("/api/borrow", wrap(WebServer::handleBorrow));
        server.createContext("/api/return", wrap(WebServer::handleReturn));
        server.createContext("/api/member", wrap(WebServer::handleMember));
        server.createContext("/api/reports/inventory", wrap(WebServer::handleInventoryReport));
        server.createContext("/api/reports/overdue", wrap(WebServer::handleOverdueReport));
        server.createContext("/api/reports/transactions", wrap(WebServer::handleAllTransactions));

        server.setExecutor(null);
        server.start();

        System.out.println("=================================================");
        System.out.println(" Library Management System API running");
        System.out.println(" http://localhost:" + PORT);
        System.out.println(" Open frontend/index.html in your browser now.");
        System.out.println(" Press Ctrl+C to stop.");
        System.out.println("=================================================");
        Logger.info("Web server started on port " + PORT);
    }

    private static void bootstrap() {
        List<User> users = FileManager.load(USERS_FILE);
        List<Book> books = FileManager.load(BOOKS_FILE);
        List<Transaction> transactions = FileManager.load(TXNS_FILE);

        authService = new AuthService(users);
        libraryService = LibraryService.getInstance(books);
        transactionService = new TransactionService(libraryService, transactions);
        reportService = new ReportService(libraryService, transactionService);

        if (users == null) {
            authService.register("LIBRARIAN", "Default Admin", "admin", "admin123");
            persistUsers();
        }
        if (books == null) {
            libraryService.addBook(new Book("ISBN001", "Clean Code", "Robert C. Martin", "Software", 3));
            libraryService.addBook(new Book("ISBN002", "Effective Java", "Joshua Bloch", "Software", 2));
            libraryService.addBook(new Book("ISBN003", "The Pragmatic Programmer", "Andy Hunt", "Software", 1));
            persistBooks();
        }
    }

    private static void persistUsers() {
        FileManager.save(USERS_FILE, authService.getAllUsers());
    }

    private static void persistBooks() {
        FileManager.save(BOOKS_FILE, libraryService.getAllBooks());
    }

    private static void persistTransactions() {
        FileManager.save(TXNS_FILE, transactionService.getAllTransactions());
    }

    // ---------- Handlers ----------

    private static void handleRegister(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, err("Method not allowed"));
            return;
        }
        Map<String, String> body = JsonUtil.parseFlatObject(readBody(ex));
        try {
            User user = authService.register(
                    body.get("role"), body.get("name"), body.get("username"), body.get("password"));
            persistUsers();
            sendJson(ex, 200, userToMap(user));
        } catch (IllegalArgumentException e) {
            sendJson(ex, 400, err(e.getMessage()));
        }
    }

    private static void handleLogin(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, err("Method not allowed"));
            return;
        }
        Map<String, String> body = JsonUtil.parseFlatObject(readBody(ex));
        try {
            User user = authService.login(body.get("username"), body.get("password"));
            sendJson(ex, 200, userToMap(user));
        } catch (InvalidCredentialsException e) {
            sendJson(ex, 401, err(e.getMessage()));
        }
    }

    private static void handleBooks(HttpExchange ex) throws IOException {
        String method = ex.getRequestMethod();
        String path = ex.getRequestURI().getPath(); // /api/books or /api/books/{isbn}
        String[] parts = path.split("/");
        String isbn = parts.length > 3 ? parts[3] : null;

        switch (method) {
            case "GET": {
                List<Map<String, Object>> result = new ArrayList<>();
                for (Book b : libraryService.getAllBooks()) {
                    result.add(bookToMap(b));
                }
                sendJson(ex, 200, result);
                break;
            }
            case "POST": {
                Map<String, String> body = JsonUtil.parseFlatObject(readBody(ex));
                try {
                    int copies = Integer.parseInt(body.getOrDefault("totalCopies", "1"));
                    Book book = new Book(body.get("isbn"), body.get("title"),
                            body.get("author"), body.get("genre"), copies);
                    libraryService.addBook(book);
                    persistBooks();
                    sendJson(ex, 200, bookToMap(book));
                } catch (Exception e) {
                    sendJson(ex, 400, err(e.getMessage()));
                }
                break;
            }
            case "PUT": {
                if (isbn == null) { sendJson(ex, 400, err("ISBN required")); return; }
                Map<String, String> body = JsonUtil.parseFlatObject(readBody(ex));
                try {
                    libraryService.updateBook(isbn, body.get("title"), body.get("author"), body.get("genre"));
                    persistBooks();
                    sendJson(ex, 200, bookToMap(libraryService.getBookByIsbn(isbn)));
                } catch (BookNotFoundException e) {
                    sendJson(ex, 404, err(e.getMessage()));
                }
                break;
            }
            case "DELETE": {
                if (isbn == null) { sendJson(ex, 400, err("ISBN required")); return; }
                try {
                    libraryService.deleteBook(isbn);
                    persistBooks();
                    sendJson(ex, 200, ok("Book deleted"));
                } catch (BookNotFoundException e) {
                    sendJson(ex, 404, err(e.getMessage()));
                }
                break;
            }
            default:
                sendJson(ex, 405, err("Method not allowed"));
        }
    }

    private static void handleSearchBooks(HttpExchange ex) throws IOException {
        Map<String, String> query = parseQuery(ex.getRequestURI().getRawQuery());
        String type = query.getOrDefault("type", "title");
        String term = query.getOrDefault("term", "");

        List<Book> matches;
        switch (type) {
            case "author": matches = libraryService.searchByAuthor(term); break;
            case "genre": matches = libraryService.searchByGenre(term); break;
            default: matches = libraryService.searchByTitle(term);
        }

        List<Map<String, Object>> result = new ArrayList<>();
        for (Book b : matches) result.add(bookToMap(b));
        sendJson(ex, 200, result);
    }

    private static void handleBorrow(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, err("Method not allowed"));
            return;
        }
        Map<String, String> body = JsonUtil.parseFlatObject(readBody(ex));
        String memberId = body.get("memberId");
        String isbn = body.get("isbn");

        Member member = getMemberOrNull(memberId);
        if (member == null) { sendJson(ex, 404, err("Member not found")); return; }

        try {
            Transaction txn = transactionService.issueBook(member, isbn);
            persistBooks();
            persistTransactions();
            persistUsers();
            sendJson(ex, 200, txnToMap(txn));
        } catch (BookNotFoundException e) {
            sendJson(ex, 404, err(e.getMessage()));
        } catch (BookNotAvailableException e) {
            sendJson(ex, 409, err(e.getMessage()));
        }
    }

    private static void handleReturn(HttpExchange ex) throws IOException {
        if (!"POST".equals(ex.getRequestMethod())) {
            sendJson(ex, 405, err("Method not allowed"));
            return;
        }
        Map<String, String> body = JsonUtil.parseFlatObject(readBody(ex));
        String memberId = body.get("memberId");
        String transactionId = body.get("transactionId");

        Member member = getMemberOrNull(memberId);
        if (member == null) { sendJson(ex, 404, err("Member not found")); return; }

        try {
            double fine = transactionService.returnBook(member, transactionId);
            persistBooks();
            persistTransactions();
            persistUsers();
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("fine", fine);
            result.put("message", fine > 0 ? "Returned late. Fine charged." : "Returned on time.");
            sendJson(ex, 200, result);
        } catch (BookNotFoundException e) {
            sendJson(ex, 404, err(e.getMessage()));
        } catch (IllegalArgumentException e) {
            sendJson(ex, 400, err(e.getMessage()));
        }
    }

    private static void handleMember(HttpExchange ex) throws IOException {
        String path = ex.getRequestURI().getPath(); // /api/member/{id}
        String[] parts = path.split("/");
        String memberId = parts.length > 3 ? parts[3] : null;

        Member member = getMemberOrNull(memberId);
        if (member == null) { sendJson(ex, 404, err("Member not found")); return; }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("userId", member.getUserId());
        result.put("name", member.getName());
        result.put("borrowedIsbns", new ArrayList<>(member.getBorrowedBookIsbns()));
        result.put("outstandingFine", member.getOutstandingFine());

        List<Map<String, Object>> txns = new ArrayList<>();
        for (Transaction t : transactionService.getTransactionsForMember(memberId)) {
            txns.add(txnToMap(t));
        }
        result.put("transactions", txns);
        sendJson(ex, 200, result);
    }

    private static void handleInventoryReport(HttpExchange ex) throws IOException {
        List<Book> books = libraryService.getAllBooks();
        int totalCopies = 0, availableCopies = 0;
        for (Book b : books) {
            totalCopies += b.getTotalCopies();
            availableCopies += b.getAvailableCopies();
        }
        Map<String, Object> result = new LinkedHashMap<>();
        result.put("distinctTitles", books.size());
        result.put("totalCopies", totalCopies);
        result.put("availableCopies", availableCopies);
        result.put("onLoan", totalCopies - availableCopies);
        sendJson(ex, 200, result);
    }

    private static void handleOverdueReport(HttpExchange ex) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction t : transactionService.getOverdueTransactions()) {
            result.add(txnToMap(t));
        }
        sendJson(ex, 200, result);
    }

    private static void handleAllTransactions(HttpExchange ex) throws IOException {
        List<Map<String, Object>> result = new ArrayList<>();
        for (Transaction t : transactionService.getAllTransactions()) {
            result.add(txnToMap(t));
        }
        sendJson(ex, 200, result);
    }

    // ---------- Helpers ----------

    private static Member getMemberOrNull(String memberId) {
        if (memberId == null) return null;
        User u = authService.getUserById(memberId);
        return (u instanceof Member) ? (Member) u : null;
    }

    private static Map<String, Object> userToMap(User u) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("userId", u.getUserId());
        m.put("name", u.getName());
        m.put("username", u.getUsername());
        m.put("role", u.getRole());
        if (u instanceof Member) {
            m.put("outstandingFine", ((Member) u).getOutstandingFine());
        }
        return m;
    }

    private static Map<String, Object> bookToMap(Book b) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("isbn", b.getIsbn());
        m.put("title", b.getTitle());
        m.put("author", b.getAuthor());
        m.put("genre", b.getGenre());
        m.put("totalCopies", b.getTotalCopies());
        m.put("availableCopies", b.getAvailableCopies());
        return m;
    }

    private static Map<String, Object> txnToMap(Transaction t) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("transactionId", t.getTransactionId());
        m.put("memberId", t.getMemberId());
        m.put("isbn", t.getIsbn());
        m.put("issueDate", t.getIssueDate().toString());
        m.put("dueDate", t.getDueDate().toString());
        m.put("returnDate", t.getReturnDate() == null ? null : t.getReturnDate().toString());
        m.put("status", t.getStatus().toString());
        m.put("fineCharged", t.getFineCharged());
        return m;
    }

    private static Map<String, Object> err(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("error", message);
        return m;
    }

    private static Map<String, Object> ok(String message) {
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("message", message);
        return m;
    }

    private static String readBody(HttpExchange ex) throws IOException {
        InputStream is = ex.getRequestBody();
        java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
        byte[] buf = new byte[1024];
        int n;
        while ((n = is.read(buf)) != -1) out.write(buf, 0, n);
        return out.toString(StandardCharsets.UTF_8.name());
    }

    private static Map<String, String> parseQuery(String rawQuery) {
        Map<String, String> map = new LinkedHashMap<>();
        if (rawQuery == null) return map;
        for (String pair : rawQuery.split("&")) {
            String[] kv = pair.split("=", 2);
            if (kv.length == 2) {
                map.put(urlDecode(kv[0]), urlDecode(kv[1]));
            }
        }
        return map;
    }

    private static String urlDecode(String s) {
        try {
            return java.net.URLDecoder.decode(s, "UTF-8");
        } catch (Exception e) {
            return s;
        }
    }

    private static void sendJson(HttpExchange ex, int statusCode, Object body) throws IOException {
        byte[] bytes = JsonUtil.toJson(body).getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", "application/json; charset=utf-8");
        ex.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        ex.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = ex.getResponseBody()) {
            os.write(bytes);
        }
    }

    /**
     * Wraps a handler with CORS preflight handling and centralized error
     * catching, so an unexpected exception returns HTTP 500 with a JSON
     * error body instead of silently dropping the connection.
     */
    private static HttpHandler wrap(HttpHandlerThrowing handler) {
        return exchange -> {
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Access-Control-Allow-Methods", "GET,POST,PUT,DELETE,OPTIONS");
            exchange.getResponseHeaders().set("Access-Control-Allow-Headers", "Content-Type");

            if ("OPTIONS".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(204, -1);
                return;
            }
            try {
                handler.handle(exchange);
            } catch (Exception e) {
                Logger.error("Unhandled error: " + e.getMessage());
                sendJson(exchange, 500, err("Internal server error: " + e.getMessage()));
            }
        };
    }

    @FunctionalInterface
    private interface HttpHandlerThrowing {
        void handle(HttpExchange exchange) throws Exception;
    }
}
