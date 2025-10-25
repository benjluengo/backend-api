package com.huertohogar.backend_api.controller;

import com.huertohogar.backend_api.model.Order;
import com.huertohogar.backend_api.model.OrderStatus;
import com.huertohogar.backend_api.model.User;
import com.huertohogar.backend_api.service.JwtService;
import com.huertohogar.backend_api.service.OrderService;
import com.huertohogar.backend_api.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*")
public class OrderController {
    private static final Logger log = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private JwtService jwtService;

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<Order>> getOrdersByUser(@PathVariable Long userId) {
        return userService.getUserById(userId)
                .map(user -> ResponseEntity.ok(orderService.getOrdersByUser(user)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> getOrderById(@PathVariable Long id) {
        return orderService.getOrderById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody Order order, @RequestHeader("Authorization") String token) {
        log.info("Recibida solicitud de creación de orden");
        try {
            if (token == null || !token.startsWith("Bearer ")) {
                log.warn("Token de autenticación inválido");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token de autenticación no válido"));
            }

            // Extraer el userId del token
            String tokenValue = token.replace("Bearer ", "");
            String userId = jwtService.extractUserId(tokenValue);

            if (userId == null) {
                log.warn("Token no contiene información de usuario");
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Token no contiene información de usuario"));
            }

            log.debug("Procesando orden para usuario ID: {}", userId);

            // Obtener el usuario
            User user = userService.getUserById(Long.parseLong(userId))
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

            // Validar items de la orden
            if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
                log.warn("Intento de crear orden sin items");
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "La orden debe tener al menos un ítem"));
            }

            // Validar que cada item tenga un producto válido
            boolean hasInvalidItems = order.getOrderItems().stream()
                .anyMatch(item -> item.getProduct() == null || item.getProduct().getId() == null);
            if (hasInvalidItems) {
                log.warn("Orden contiene items inválidos");
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "La orden contiene productos inválidos"));
            }

            // Validar fecha de entrega
            if (order.getDeliveryDate() == null) {
                log.warn("Fecha de entrega no proporcionada");
                return ResponseEntity.badRequest()
                    .body(Map.of("message", "La fecha de entrega es requerida"));
            }

            log.debug("Preparando orden con {} items", order.getOrderItems().size());

            // Asignar el usuario y establecer relaciones
            order.setUser(user);
            order.getOrderItems().forEach(item -> item.setOrder(order));

            // Crear la orden
            Order newOrder = orderService.createOrder(order);
            log.info("Orden creada exitosamente con ID: {}", newOrder.getId());

            return ResponseEntity.ok(newOrder);

        } catch (NumberFormatException e) {
            log.error("Error al procesar ID de usuario: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", "ID de usuario inválido"));
        } catch (RuntimeException e) {
            log.error("Error al crear orden: {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("message", e.getMessage()));
        }
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(
            @PathVariable Long id, 
            @RequestBody Map<String, String> status) {
        try {
            OrderStatus newStatus = OrderStatus.valueOf(status.get("status").toUpperCase());
            Order updatedOrder = orderService.updateOrderStatus(id, newStatus);
            return updatedOrder != null ? 
                    ResponseEntity.ok(updatedOrder) : 
                    ResponseEntity.notFound().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/all")
    public List<Order> getAllOrders() {
        return orderService.getAllOrders();
    }
}