package com.huertohogar.backend_api.config;

import com.huertohogar.backend_api.model.Product;
import com.huertohogar.backend_api.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Arrays;
import java.util.List;

@Configuration
public class DataLoader {

    @Bean
    CommandLineRunner initDatabase(ProductRepository productRepository) {
        return args -> {
            List<Product> products = Arrays.asList(
                // Frutas
                new Product(null, "Manzana Fuji", 1200.0, "../images/manzana-fuji.jpg", "Frutas", 150,
                    "Manzanas Fuji crujientes y dulces, cultivadas en el Valle del Maule.", 
                    "Valle del Maule, Chile", "Certificado orgánico, riego eficiente",
                    Arrays.asList("Ensalada de manzana", "Tarta de manzana")),
                new Product(null, "Naranjas Valencia", 1000.0, "../images/naranja-valencia.jpg", "Frutas", 200,
                    "Jugosas y ricas en vitamina C, estas naranjas Valencia son ideales para zumos frescos y refrescantes.", 
                    "Región de O'Higgins, Chile", "Agricultura sostenible, sin pesticidas",
                    Arrays.asList("Zumo de naranja", "Ensalada cítrica")),
                new Product(null, "Plátanos Cavendish", 800.0, "../images/platano-cavendish.jpg", "Frutas", 250,
                    "Plátanos maduros y dulces, perfectos para el desayuno o como snack energético.", 
                    "Costa Central, Chile", "Cultivo orgánico",
                    Arrays.asList("Plátano con miel", "Batido de plátano")),
                
                // Verduras
                new Product(null, "Zanahorias Orgánicas", 900.0, "../images/zanahoria-organica.jpg", "Verduras", 100,
                    "Zanahorias crujientes cultivadas sin pesticidas en la Región de O'Higgins.", 
                    "Región Metropolitana, Chile", "Orgánico certificado",
                    Arrays.asList("Zanahorias glaseadas", "Sopa de zanahoria")),
                new Product(null, "Espinacas Frescas", 700.0, "../images/espinaca-fresca.jpg", "Verduras", 80,
                    "Espinacas frescas y nutritivas, perfectas para ensaladas y batidos verdes.", 
                    "Valle Central, Chile", "Cultivo hidropónico sostenible",
                    Arrays.asList("Ensalada de espinacas", "Batido verde")),
                new Product(null, "Pimientos Tricolores", 1500.0, "../images/pimiento-tricolor.jpg", "Verduras", 120,
                    "Pimientos rojos, amarillos y verdes, ideales para salteados y platos coloridos.", 
                    "Región del Maule, Chile", "Agricultura integrada",
                    Arrays.asList("Pimientos rellenos", "Ensalada de pimientos")),
                
                // Productos Orgánicos
                new Product(null, "Miel Orgánica", 5000.0, "../images/miel-organica.jpg", "Productos Orgánicos", 40,
                    "Miel pura y orgánica producida por apicultores locales.", 
                    "Región de Los Lagos, Chile", "Apicultura orgánica",
                    Arrays.asList("Té con miel", "Aderezo de miel")),
                new Product(null, "Quinoa Orgánica", 6000.0, "../images/quinoa-organica.jpg", "Productos Orgánicos", 40,
                    "Quinoa 100% orgánica, rica en proteínas, ideal para dietas saludables y libre de gluten. Cultivada sin pesticidas ni químicos.", 
                    "Altiplano chileno", "Cultivo ancestral sostenible",
                    Arrays.asList("Quinoa con verduras", "Ensalada de quinoa")),
                
                // Productos Lácteos
                new Product(null, "Leche Entera", 1100.0, "../images/leche-entera.jpg", "Productos Lácteos", 60,
                    "Leche entera pasteurizada de alta calidad, fuente natural de calcio y vitaminas. Perfecta para consumo diario.", 
                    "Región de Los Lagos, Chile", "Ganadería sostenible",
                    Arrays.asList("Café con leche", "Cereal con leche"))
            );

            productRepository.saveAll(products);
        };
    }
}