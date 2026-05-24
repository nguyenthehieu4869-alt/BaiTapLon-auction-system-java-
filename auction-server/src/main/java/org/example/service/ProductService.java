package org.example.service;

import org.example.model.Product;
import org.example.database.ProductDAO;

import java.util.List;

public class ProductService {

    private ProductDAO dao = new ProductDAO();

    public List<Product> getAllProducts() {
        return dao.getAllProducts();
    }

    public List<Product> getProductsBySeller(String sellerUsername) {
        return dao.getProductsBySeller(sellerUsername);
    }
}

