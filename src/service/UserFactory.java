package service;

import model.Librarian;
import model.Member;
import model.User;
import util.IdGenerator;
import util.PasswordUtil;

/**
 * Factory Design Pattern: centralizes the creation logic for different
 * User subtypes so client code never calls "new Librarian(...)" or
 * "new Member(...)" directly.
 */
public class UserFactory {

    public static User createUser(String role, String name, String username, String plainPassword) {
        String id = IdGenerator.nextUserId();
        String hashed = PasswordUtil.hash(plainPassword);

        switch (role.toUpperCase()) {
            case "LIBRARIAN":
                return new Librarian(id, name, username, hashed);
            case "MEMBER":
                return new Member(id, name, username, hashed);
            default:
                throw new IllegalArgumentException("Unknown role: " + role);
        }
    }
}
