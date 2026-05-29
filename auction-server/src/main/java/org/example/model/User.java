package org.example.model;

public abstract class User extends Entity {

    private final String username;
    private final String password;
    private final String email;
    private final UserRole role;

    protected User(int id, String username, String password, String email, UserRole role) {
        super(id);
        this.username = username;
        this.password = password;
        this.email = email;
        this.role = role;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }

    public UserRole getRole() {
        return role;
    }

    @Override
    public String printInfo() {
        return role + ": " + username;
    }
}
