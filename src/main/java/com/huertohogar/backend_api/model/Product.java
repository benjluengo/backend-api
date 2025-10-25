package com.huertohogar.backend_api.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private double price;

    @Column(nullable = false)
    private String image;

    @Column(nullable = false)
    private String category;

    @Column(nullable = false)
    private int stock;

    @Column(columnDefinition = "TEXT")
    private String description;

    private String origin;

    @Column(name = "sustainable_practices")
    private String sustainablePractices;

    @ElementCollection
    @CollectionTable(name = "product_suggested_recipes", 
                    joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "recipe")
    private List<String> suggestedRecipes;
}