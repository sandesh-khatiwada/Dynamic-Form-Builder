package com.sandesh.formbuilder.repository;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public class UserRepositoryCustomImpl implements UserRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    @Transactional
    public void insertUserWithRoles(String email, String username, String password, String roleName) {
        String sql = "INSERT INTO users (id, email, username, password) VALUES (:id, :email, :username, :password);" +
                "INSERT INTO users_roles (user_id, role_id) VALUES (:userId, (SELECT id FROM role WHERE name = :roleName))";
        UUID userId = UUID.randomUUID();
        entityManager.createNativeQuery(sql)
                .setParameter("id", userId)
                .setParameter("email", email)
                .setParameter("username", username)
                .setParameter("password", password)
                .setParameter("userId", userId)
                .setParameter("roleName", roleName)
                .executeUpdate();
    }
}