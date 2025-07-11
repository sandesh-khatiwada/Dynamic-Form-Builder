package com.sandesh.formbuilder.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class FormResponse {

    private UUID templateId;
    private String name;
    private List<Map<String, Object>> jsonSchema;
    private LocalDateTime createdAt;
    private boolean allowEdit;
    private boolean allowDelete;


}
