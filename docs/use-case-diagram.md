# Use Case Diagram

```mermaid
flowchart LR
    Librarian([Librarian])
    Member([Member])

    subgraph System["Library Management System"]
        UC1[Register / Login]
        UC2[Add Book]
        UC3[Update Book]
        UC4[Delete Book]
        UC5[Search Book]
        UC6[Borrow Book]
        UC7[Return Book]
        UC8[View Reports]
        UC9[View Borrow History and Fines]
    end

    Librarian --> UC1
    Librarian --> UC2
    Librarian --> UC3
    Librarian --> UC4
    Librarian --> UC8

    Member --> UC1
    Member --> UC5
    Member --> UC6
    Member --> UC7
    Member --> UC9
```

## Description

| Actor | Use Cases |
|---|---|
| **Librarian** | Register/Login, Add Book, Update Book, Delete Book, View Reports (inventory, overdue, all transactions) |
| **Member** | Register/Login, Search Book, Borrow Book, Return Book, View Borrow History & Fines |

Both actors share the **Register/Login** use case, which is handled by
`AuthService` and routes each user to a role-specific menu based on the
polymorphic `getRole()` result.
