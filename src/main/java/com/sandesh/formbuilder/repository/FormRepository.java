package com.sandesh.formbuilder.repository;

import com.sandesh.formbuilder.entity.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FormRepository extends JpaRepository<FormTemplate, UUID> {
    @Query(value = "SELECT * FROM form_template WHERE LOWER(name) LIKE LOWER(CONCAT('%', ?1, '%')) ORDER BY created_at DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<FormTemplate> findByNameWithOffsetAndLimit(String name, int offset, int limit);

    @Query(value = "SELECT * FROM form_template ORDER BY created_at DESC LIMIT ?2 OFFSET ?1", nativeQuery = true)
    List<FormTemplate> findAllWithOffsetAndLimit(int offset, int limit);
}
