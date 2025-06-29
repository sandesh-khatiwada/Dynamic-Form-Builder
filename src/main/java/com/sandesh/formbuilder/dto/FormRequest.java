package com.sandesh.formbuilder.dto;

import com.sandesh.formbuilder.validation.ValidJsonSchema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class FormRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotNull(message = "JSON schema is required")
    @ValidJsonSchema
    private List<Map<String, Object>> jsonSchema;
}
