package org.example.service;

import org.example.database.UserDAO;

public class UserService {

    private UserDAO dao = new UserDAO();

    public boolean login(String username, String password) {
        return dao.login(username, password);
    }
}

