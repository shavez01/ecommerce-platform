
package com.ecommerce.monolith.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.ecommerce.monolith.order.entity.Order;

public interface OrderRepository extends JpaRepository<Order, Long> {

}
