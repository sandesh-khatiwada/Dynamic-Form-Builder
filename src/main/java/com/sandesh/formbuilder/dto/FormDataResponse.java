package com.sandesh.formbuilder.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Data
public class FormDataResponse {
    private UUID formDataId;
    private List<Map<String, Object>> jsonData;
}
