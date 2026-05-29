package org.example.model;

public class Bidder extends User {
    public Bidder(int id, String username, String password, String email) {
        super(id, username, password, email, UserRole.BIDDER);
    }
}
