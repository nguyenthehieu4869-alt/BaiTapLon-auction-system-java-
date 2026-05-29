package org.example.model;

public class Seller extends User {
    public Seller(int id, String username, String password, String email) {
        super(id, username, password, email, UserRole.SELLER);
    }
}
