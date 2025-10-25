package com.huertohogar.backend_api.repository;

import com.huertohogar.backend_api.model.Order;
import com.huertohogar.backend_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
    List<Order> findByUserOrderByOrderDateDesc(User user);
}