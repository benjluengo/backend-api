package com.huertohogar.backend_api.service;

import com.huertohogar.backend_api.model.Product;
import com.huertohogar.backend_api.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductService {

    @Autowired
    private ProductRepository productRepository;

    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getProductsByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public List<Product> searchProducts(String query) {
        return productRepository.findByNameContainingIgnoreCase(query);
    }

    public Product createProduct(Product product) {
        return productRepository.save(product);
    }

    public Product updateProduct(Long id, Product productDetails) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product existingProduct = product.get();
            existingProduct.setName(productDetails.getName());
            existingProduct.setPrice(productDetails.getPrice());
            existingProduct.setImage(productDetails.getImage());
            existingProduct.setCategory(productDetails.getCategory());
            existingProduct.setStock(productDetails.getStock());
            existingProduct.setDescription(productDetails.getDescription());
            existingProduct.setOrigin(productDetails.getOrigin());
            existingProduct.setSustainablePractices(productDetails.getSustainablePractices());
            existingProduct.setSuggestedRecipes(productDetails.getSuggestedRecipes());
            return productRepository.save(existingProduct);
        }
        return null;
    }

    public boolean deleteProduct(Long id) {
        if (productRepository.existsById(id)) {
            productRepository.deleteById(id);
            return true;
        }
        return false;
    }

    public boolean updateStock(Long id, int quantity) {
        Optional<Product> product = productRepository.findById(id);
        if (product.isPresent()) {
            Product existingProduct = product.get();
            int newStock = existingProduct.getStock() - quantity;
            if (newStock >= 0) {
                existingProduct.setStock(newStock);
                productRepository.save(existingProduct);
                return true;
            }
        }
        return false;
    }
}