package com.sandesh.formbuilder.repository;

import com.sandesh.formbuilder.entity.FormData;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface FormDataRepository extends JpaRepository<FormData, UUID> {
    List<FormData> findByFormTemplateId(UUID formTemplateId);
}
