# Class Diagram

```mermaid
classDiagram
    class User {
        <<abstract>>
        -String userId
        -String name
        -String username
        -String passwordHash
        +getRole()* String
        +getPermissions()* String[]
    }

    class Librarian {
        +getRole() String
        +getPermissions() String[]
    }

    class Member {
        -List~String~ borrowedBookIsbns
        -double outstandingFine
        +addBorrowedBook(isbn)
        +removeBorrowedBook(isbn)
        +addFine(amount)
    }

    class Book {
        -String isbn
        -String title
        -String author
        -String genre
        -int totalCopies
        -int availableCopies
        +isAvailable() boolean
        +decrementAvailable()
        +incrementAvailable()
    }

    class Transaction {
        -String transactionId
        -String memberId
        -String isbn
        -LocalDate issueDate
        -LocalDate dueDate
        -LocalDate returnDate
        -Status status
        -double fineCharged
        +markReturned(date, fine)
    }

    class Searchable {
        <<interface>>
        +searchByTitle(title) List~Book~
        +searchByAuthor(author) List~Book~
        +searchByGenre(genre) List~Book~
    }

    class Renewable {
        <<interface>>
        +renew(transactionId, extraDays) boolean
    }

    class AuthService {
        -Map~String,User~ usersByUsername
        +register(role, name, username, password) User
        +login(username, password) User
    }

    class LibraryService {
        <<Singleton>>
        -Map~String,Book~ catalog
        +getInstance() LibraryService
        +addBook(book)
        +updateBook(isbn, ...)
        +deleteBook(isbn)
    }

    class TransactionService {
        -Map~String,Transaction~ transactions
        +issueBook(member, isbn) Transaction
        +returnBook(member, transactionId) double
        +calculateFine(dueDate, returnDate) double
    }

    class UserFactory {
        <<Factory>>
        +createUser(role, name, username, password)$ User
    }

    class ReportService {
        +printInventorySummary()
        +printOverdueReport()
        +printAllTransactions()
    }

    User <|-- Librarian
    User <|-- Member
    LibraryService ..|> Searchable
    TransactionService ..|> Renewable
    AuthService --> User : manages
    LibraryService --> Book : manages
    TransactionService --> Transaction : manages
    TransactionService --> LibraryService : uses
    ReportService --> LibraryService : uses
    ReportService --> TransactionService : uses
    UserFactory --> User : creates
    AuthService --> UserFactory : uses
```

## Notes

- `User` is abstract; `Librarian` and `Member` provide concrete, polymorphic
  implementations of `getRole()` and `getPermissions()`.
- `LibraryService` is implemented as a **Singleton** — the whole application
  shares one catalog instance.
- `UserFactory` implements the **Factory** pattern, hiding the decision of
  whether to instantiate a `Librarian` or `Member`.
- `Searchable` and `Renewable` are interfaces that decouple *what* a service
  can do from *how* it does it.
