package com.sandesh.formbuilder.repository;

import com.sandesh.formbuilder.entity.FormTemplate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface FormRepository extends JpaRepository<FormTemplate, UUID> {
}
