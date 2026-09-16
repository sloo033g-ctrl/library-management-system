# Problem Statement

## Problem Statement

Small and mid-sized libraries (college departments, community reading rooms,
office libraries) often track book inventory and lending manually using
registers or spreadsheets. This leads to:

- No real-time visibility into which copies are available
- No systematic way to track who currently holds which book, or for how long
- Manual, error-prone fine calculation for overdue returns
- No audit trail of who added, removed, or modified catalog entries

The **Library Management System** addresses this by providing a structured,
role-based console application where librarians manage the catalog and
members can search, borrow, and return books, with all transactions and fines
tracked automatically and persisted between sessions.

## Scope of the Project

**In scope:**
- Single-library, single-branch catalog management
- Role-based access for Librarians and Members
- Book CRUD operations, search, issuing/returning, and fine calculation
- Local file-based persistence (no external database or network server)
- Basic reporting (inventory summary, overdue list, transaction history)

**Out of scope:**
- Multi-branch / multi-library federation
- Graphical user interface or web interface
- Payment gateway integration for fine settlement
- Notifications (email/SMS) for due dates

## Target Users

1. **Librarians** — responsible for maintaining the book catalog, issuing and
   receiving books, and reviewing inventory/overdue reports.
2. **Members** — students, staff, or readers who search the catalog and
   borrow/return books.

## High-Level Features

- Secure registration and login with hashed passwords
- Add, update, delete, and search books by title/author/genre
- Issue books with an automatic 14-day due date
- Return books with automatic overdue fine calculation
- View borrowing history and outstanding fines per member
- Inventory, overdue, and transaction reports for librarians
- All data persisted locally so state survives application restarts
