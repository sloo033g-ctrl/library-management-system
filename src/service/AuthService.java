package service;

import exception.InvalidCredentialsException;
import model.User;
import util.Logger;
import util.PasswordUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Handles registration and authentication.
 * Module: User Management.
 * Uses a HashMap keyed by username for O(1) lookups (Performance NFR).
 */
public class AuthService {

    private Map<String, User> usersByUsername;

    public AuthService(List<User> initialUsers) {
        usersByUsername = new HashMap<>();
        if (initialUsers != null) {
            for (User u : initialUsers) {
                usersByUsername.put(u.getUsername(), u);
            }
        }
    }

    public User register(String role, String name, String username, String plainPassword) {
        if (usersByUsername.containsKey(username)) {
            throw new IllegalArgumentException("Username already exists: " + username);
        }
        User user = UserFactory.createUser(role, name, username, plainPassword);
        usersByUsername.put(username, user);
        Logger.info("Registered new " + role + ": " + username);
        return user;
    }

    public User login(String username, String plainPassword) throws InvalidCredentialsException {
        User user = usersByUsername.get(username);
        if (user == null || !PasswordUtil.verify(plainPassword, user.getPasswordHash())) {
            Logger.warn("Failed login attempt for username: " + username);
            throw new InvalidCredentialsException("Invalid username or password.");
        }
        Logger.info("User logged in: " + username);
        return user;
    }

    public List<User> getAllUsers() {
        return new java.util.ArrayList<>(usersByUsername.values());
    }

    public User getUserById(String userId) {
        for (User u : usersByUsername.values()) {
            if (u.getUserId().equals(userId)) {
                return u;
            }
        }
        return null;
    }
}
