package com.sandesh.formbuilder.dto;

import lombok.Data;

import java.util.List;
import java.util.Map;


@Data
public class FormDataRequest {
    private List<Map<String, Object>> jsonData;
}
