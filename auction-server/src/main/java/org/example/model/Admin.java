package org.example.model;

public class Admin extends User {
    public Admin(int id, String username, String password, String email) {
        super(id, username, password, email, UserRole.ADMIN);
    }
}
