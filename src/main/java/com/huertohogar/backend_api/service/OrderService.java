package com.huertohogar.backend_api.service;

import com.huertohogar.backend_api.model.Order;
import com.huertohogar.backend_api.model.OrderItem;
import com.huertohogar.backend_api.model.OrderStatus;
import com.huertohogar.backend_api.model.Product;
import com.huertohogar.backend_api.model.User;
import com.huertohogar.backend_api.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductService productService;

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUserOrderByOrderDateDesc(user);
    }

    public Optional<Order> getOrderById(Long id) {
        return orderRepository.findById(id);
    }

    @Transactional(rollbackFor = Exception.class)
    public Order createOrder(Order order) {
        log.info("Iniciando creación de orden con {} items", order.getOrderItems().size());
        try {
            // Establecer fecha y estado
            order.setOrderDate(LocalDateTime.now());
            order.setStatus(OrderStatus.PENDING);

            log.debug("Procesando items de la orden");
            // Establecer la relación bidireccional y validar stock
            for (OrderItem item : order.getOrderItems()) {
                if (item.getProduct() == null || item.getProduct().getId() == null) {
                    throw new RuntimeException("Producto inválido en la orden");
                }

                item.setOrder(order);
                log.debug("Verificando stock para producto ID: {}", item.getProduct().getId());

                // Cargar el producto completo para acceder a su nombre
                Product product = productService.getProductById(item.getProduct().getId())
                    .orElseThrow(() -> new RuntimeException("Producto no encontrado: " + item.getProduct().getId()));

                // Verificar y actualizar el stock de productos
                boolean stockUpdated = productService.updateStock(item.getProduct().getId(), item.getQuantity());
                if (!stockUpdated) {
                    throw new RuntimeException("No hay suficiente stock para " + product.getName());
                }
            }

            log.debug("Guardando orden en la base de datos");
            Order savedOrder = orderRepository.save(order);
            log.info("Orden creada exitosamente con ID: {}", savedOrder.getId());

            return savedOrder;

        } catch (Exception e) {
            log.error("Error al crear la orden: {}", e.getMessage());
            throw new RuntimeException("Error al crear la orden: " + e.getMessage());
        }
    }

    public Order updateOrderStatus(Long id, OrderStatus status) {
        Optional<Order> order = orderRepository.findById(id);
        if (order.isPresent()) {
            Order existingOrder = order.get();
            existingOrder.setStatus(status);
            return orderRepository.save(existingOrder);
        }
        return null;
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }
}