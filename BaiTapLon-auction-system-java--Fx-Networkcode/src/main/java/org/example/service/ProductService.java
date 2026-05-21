package org.example.service;

import org.example.database.ProductDAO;

import java.util.List;

public class ProductService {

    private ProductDAO dao = new ProductDAO();

    public List<String> getAllProducts() {
        return dao.getAllProducts();
    }
}

