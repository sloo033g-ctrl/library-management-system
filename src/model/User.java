package model;

import java.io.Serializable;

/**
 * Abstract base class representing any user of the Library system.
 * Demonstrates: Abstraction, Encapsulation.
 */
public abstract class User implements Serializable {
    private static final long serialVersionUID = 1L;

    protected String userId;
    protected String name;
    protected String username;
    protected String passwordHash;

    public User(String userId, String name, String username, String passwordHash) {
        this.userId = userId;
        this.name = name;
        this.username = username;
        this.passwordHash = passwordHash;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getUsername() {
        return username;
    }

    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Every concrete user type must describe its own role.
     * Demonstrates: Polymorphism (overridden differently by subclasses).
     */
    public abstract String getRole();

    /**
     * Every concrete user type defines what actions it is permitted to do.
     */
    public abstract String[] getPermissions();

    @Override
    public String toString() {
        return String.format("[%s] %s (username: %s)", getRole(), name, username);
    }
}
