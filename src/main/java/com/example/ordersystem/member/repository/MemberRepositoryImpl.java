package com.example.ordersystem.member.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public class MemberRepositoryImpl implements MemberRepositoryCustom {
    @Autowired
    private EntityManager entityManager;

    @Transactional
    public void softDeleteByEmail(String email) {
        String jpql = "UPDATE Member m SET m.delYn = :delYn WHERE m.email = :email";
        Query query = entityManager.createQuery(jpql);
        query.setParameter("delYn", "Y");
        query.setParameter("email", email);
        query.executeUpdate();
    }
}
