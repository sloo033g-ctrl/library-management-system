<img width="596" height="325" alt="Screenshot 2026-09-17 at 2 03 34 PM" src="https://github.com/user-attachments/assets/fd116d8c-130d-494f-84ad-7a1f273c8a2d" />


# Library Management System

A console-based Library Management System built in core Java to demonstrate
Object-Oriented Programming principles, design patterns, and clean software
engineering practices.

## Overview

The system allows a **Librarian** to manage a book catalog and a **Member**
to search, borrow, and return books. All data is persisted to disk between
runs using Java object serialization, so nothing is lost when the program
restarts.

## Features

### 1. User Management
- Registration and login for two roles: `LIBRARIAN` and `MEMBER`
- Passwords are hashed (SHA-256) before storage — plaintext passwords are
  never saved or compared directly
- A default librarian account (`admin` / `admin123`) is seeded on first run

### 2. Book / Inventory Management
- Add, update, delete, and list books
- Search books by title, author, or genre
- Real-time tracking of total vs. available copies

### 3. Transaction Management
- Issue a book to a member (14-day loan period)
- Return a book with automatic overdue fine calculation (₹5/day late)
- View overdue transactions and full transaction history

### Reporting
- Inventory summary (titles, total copies, copies on loan)
- Overdue report
- Full transaction log

## Two ways to run this project

1. **Console app** (`Main.java`) — the original text-menu interface.
2. **Web app** (`WebServer.java` + `frontend/index.html`) — the same business
   logic exposed as a REST API, with a browser-based frontend on top. Both
   entry points share the same `service`/`model`/`exception` layer and the
   same data files, so either one can be used interchangeably.

## Technologies / Tools Used

- **Language:** Java 21 (compatible with Java 11+)
- **Backend web layer:** `com.sun.net.httpserver` (built into the JDK — no
  external frameworks or dependencies)
- **Frontend:** Vanilla HTML/CSS/JavaScript, single file, no build step
- **Persistence:** Java Serialization (flat files under `data/`)
- **Build:** Plain `javac` / `java` (no external build tool required)
- **Version control:** Git

## OOP Concepts & Design Patterns Used

| Concept / Pattern | Where |
|---|---|
| Abstraction | `User` abstract class, `Searchable` / `Renewable` interfaces |
| Inheritance | `Librarian`, `Member` extend `User` |
| Polymorphism | `getRole()`, `getPermissions()` overridden per subclass |
| Encapsulation | All model fields are private with controlled getters/setters |
| Singleton Pattern | `LibraryService` — one catalog instance app-wide |
| Factory Pattern | `UserFactory` — centralizes creation of `Librarian`/`Member` |
| Custom Exceptions | `BookNotFoundException`, `BookNotAvailableException`, `InvalidCredentialsException`, `UserNotFoundException` |
| Collections | `HashMap` for O(1) catalog/user lookups, `ArrayList` for lists |

## Project Structure

```
LibraryManagementSystem/
├── src/
│   ├── Main.java                  # Entry point, console menu
│   ├── model/                     # Domain entities
│   │   ├── User.java
│   │   ├── Librarian.java
│   │   ├── Member.java
│   │   ├── Book.java
│   │   └── Transaction.java
│   ├── service/                   # Business logic
│   │   ├── AuthService.java
│   │   ├── LibraryService.java
│   │   ├── TransactionService.java
│   │   ├── ReportService.java
│   │   └── UserFactory.java
│   ├── interfaces/                # Contracts
│   │   ├── Searchable.java
│   │   └── Renewable.java
│   ├── exception/                 # Custom exceptions
│   │   ├── BookNotFoundException.java
│   │   ├── BookNotAvailableException.java
│   │   ├── InvalidCredentialsException.java
│   │   └── UserNotFoundException.java
│   └── util/                      # Cross-cutting utilities
│       ├── FileManager.java
│       ├── Logger.java
│       ├── PasswordUtil.java
│       └── IdGenerator.java
│   └── web/                        # REST API layer
│       ├── WebServer.java
│       └── JsonUtil.java
├── frontend/
│   └── index.html                  # Browser UI (talks to WebServer's API)
├── data/                          # Serialized data files (generated at runtime)
├── docs/                          # Design diagrams
│   ├── class-diagram.md
│   ├── use-case-diagram.md
│   ├── sequence-diagram.md
│   └── workflow-diagram.md
├── statement.md                   # Problem statement
└── README.md
```

## Non-Functional Requirements

| Requirement | How it's addressed |
|---|---|
| **Performance** | `HashMap`-based lookups for books (by ISBN) and users (by username) give O(1) average access instead of linear scans |
| **Security** | Passwords are SHA-256 hashed via `PasswordUtil`; plaintext is never stored |
| **Reliability** | All state is persisted to disk via `FileManager` on graceful shutdown; custom checked exceptions prevent invalid operations (e.g., issuing an unavailable book) |
| **Usability** | Simple, guided console menus for every role with clear prompts and error messages |
| **Maintainability** | Layered package structure (`model` / `service` / `interfaces` / `exception` / `util`) keeps concerns separated |
| **Logging/Monitoring** | `Logger` utility timestamps and records every significant action (logins, book changes, issues/returns) to `data/application.log` |

## Steps to Install & Run

### Prerequisites
- JDK 11 or later installed (`java -version` to check)

### Compile
```bash
cd LibraryManagementSystem
mkdir -p bin
javac -d bin $(find src -name "*.java")
```

### Run the console app
```bash
java -cp bin Main
```

### Run the web app
```bash
java -cp bin web.WebServer
```
This starts a REST API on `http://localhost:8080`. Then open
`frontend/index.html` directly in a browser (double-click it, or
`open frontend/index.html` on macOS). No web server is needed to serve the
HTML file itself — it runs as a local file and talks to the API over
`fetch()`. Keep the terminal running `WebServer` open while you use the site;
closing it stops the API.

### Default login (both apps share the same data)
- Username: `admin`
- Password: `admin123`

On first run, sample books are seeded automatically. Register a `MEMBER`
account to try borrowing/returning books.

### REST API reference

| Method | Endpoint | Purpose |
|---|---|---|
| POST | `/api/register` | Create a Librarian or Member account |
| POST | `/api/login` | Authenticate and get user details |
| GET | `/api/books` | List all books |
| GET | `/api/books/search?type=title\|author\|genre&term=...` | Search books |
| POST | `/api/books` | Add a book (librarian) |
| PUT | `/api/books/{isbn}` | Update a book (librarian) |
| DELETE | `/api/books/{isbn}` | Remove a book (librarian) |
| POST | `/api/borrow` | Issue a book to a member |
| POST | `/api/return` | Return a book, calculates fine |
| GET | `/api/member/{memberId}` | Member's borrowed books, fine, history |
| GET | `/api/reports/inventory` | Inventory summary |
| GET | `/api/reports/overdue` | Overdue transactions |
| GET | `/api/reports/transactions` | Full transaction history |

All responses are JSON. All endpoints allow cross-origin requests so the
frontend can call them when opened as a local file.

## Instructions for Testing

Manual test scenarios to walk through:

1. **Registration & Login**
   - Register a new `MEMBER` account, then log in with the same credentials.
   - Attempt login with a wrong password → should show "Invalid username or password."

2. **Book Management (as Librarian)**
   - Log in as `admin` / `admin123`.
   - Add a new book, update its details, then delete it.
   - Confirm deleting a non-existent ISBN raises a clear error instead of crashing.

3. **Borrowing & Returning (as Member)**
   - Search for a book by title/author/genre.
   - Borrow an available book and note the transaction ID and due date.
   - Attempt to borrow the same book again if copies run out → should raise
     "Book is currently unavailable."
   - Return the book using the transaction ID; if returned late, a fine is
     calculated automatically.

4. **Persistence**
   - Exit the application via the main menu ("Exit").
   - Restart the program — all books, users, and transactions should still
     be present (loaded from `data/*.dat`).

5. **Reports (as Librarian)**
   - View the Inventory Summary, Overdue Report, and All Transactions Report.


<img width="595" height="315" alt="Screenshot 2026-09-17 at 2 03 50 PM" src="https://github.com/user-attachments/assets/8d2bfcac-6907-4b92-80f7-b675caabe723" />
