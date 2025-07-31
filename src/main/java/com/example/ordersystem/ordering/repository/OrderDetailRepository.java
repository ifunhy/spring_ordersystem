package com.example.ordersystem.ordering.repository;

import com.example.ordersystem.ordering.domain.OrderingDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderDetailRepository extends JpaRepository<OrderingDetail, String> {
}
