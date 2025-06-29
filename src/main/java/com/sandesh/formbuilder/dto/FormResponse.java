package com.sandesh.formbuilder.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class FormResponse {

    private UUID id;
    private String name;
    private List<Map<String, Object>> jsonSchema;

}
