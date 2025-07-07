package com.sandesh.formbuilder.repository;

import com.sandesh.formbuilder.entity.FormData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.UUID;

public interface FormDataRepository extends JpaRepository<FormData, UUID> {
    List<FormData> findByFormTemplateId(UUID formTemplateId);

    @Query(value = "SELECT * FROM form_data WHERE form_template_id = ?1 ORDER BY created_at DESC LIMIT ?3 OFFSET ?2", nativeQuery = true)
    List<FormData> findByFormTemplateIdWithOffsetAndLimit(UUID templateId, int offset, int limit);
}
