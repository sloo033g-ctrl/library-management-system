package model;

/**
 * Librarian - a User with administrative permissions.
 * Demonstrates: Inheritance, Polymorphism.
 */
public class Librarian extends User {
    private static final long serialVersionUID = 1L;

    public Librarian(String userId, String name, String username, String passwordHash) {
        super(userId, name, username, passwordHash);
    }

    @Override
    public String getRole() {
        return "LIBRARIAN";
    }

    @Override
    public String[] getPermissions() {
        return new String[] {
            "ADD_BOOK", "UPDATE_BOOK", "DELETE_BOOK",
            "ISSUE_BOOK", "RETURN_BOOK", "VIEW_REPORTS", "REGISTER_MEMBER"
        };
    }
}
