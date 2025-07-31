package com.example.ordersystem.ordering.repository;

import com.example.ordersystem.member.domain.Member;
import com.example.ordersystem.ordering.domain.Ordering;
import com.example.ordersystem.product.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderingRepository extends JpaRepository<Ordering, Long> {
//    public void save(Ordering ordering);
    List<Ordering> findAllByMember(Member member);

}
