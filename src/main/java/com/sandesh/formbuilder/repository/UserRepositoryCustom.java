package com.sandesh.formbuilder.repository;

public interface UserRepositoryCustom {
    void insertUserWithRoles(String email, String username, String password, String roleName);
}