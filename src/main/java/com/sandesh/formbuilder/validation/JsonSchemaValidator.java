
        package com.sandesh.formbuilder.validation;

import com.sandesh.formbuilder.enums.FormFieldType;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

import java.util.List;
import java.util.Map;

public class JsonSchemaValidator implements ConstraintValidator<ValidJsonSchema, List<Map<String, Object>>> {

    @Override
    public boolean isValid(List<Map<String, Object>> jsonSchema, ConstraintValidatorContext context) {
        if (jsonSchema == null || jsonSchema.isEmpty()) {
            context.disableDefaultConstraintViolation();
            context.buildConstraintViolationWithTemplate("JSON schema cannot be null or empty")
                    .addConstraintViolation();
            return false;
        }

        for (int i = 0; i < jsonSchema.size(); i++) {
            Map<String, Object> field = jsonSchema.get(i);

            // Check mandatory fields: label, type, key
            if (!field.containsKey("label") || field.get("label") == null || ((String) field.get("label")).trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Field at index " + i + ": label is required")
                        .addConstraintViolation();
                return false;
            }

            if (!field.containsKey("type") || field.get("type") == null) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Field at index " + i + ": type is required")
                        .addConstraintViolation();
                return false;
            }

            if (!field.containsKey("key") || field.get("key") == null || ((String) field.get("key")).trim().isEmpty()) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Field at index " + i + ": key is required")
                        .addConstraintViolation();
                return false;
            }

            // Validate type against allowed enum values
            String type = (String) field.get("type");
            try {
                FormFieldType.valueOf(type.toUpperCase());
            } catch (IllegalArgumentException e) {
                context.disableDefaultConstraintViolation();
                context.buildConstraintViolationWithTemplate("Field at index " + i + ": type must be one of " +
                                String.join(", ", FormFieldType.values()[0].toString(), FormFieldType.values()[1].toString(), FormFieldType.values()[2].toString(), FormFieldType.values()[3].toString(), FormFieldType.values()[4].toString(), FormFieldType.values()[5].toString(), FormFieldType.values()[6].toString(), FormFieldType.values()[7].toString(), FormFieldType.values()[8].toString()))
                        .addConstraintViolation();
                return false;
            }

            // Additional validation for specific types
            switch (type.toUpperCase()) {
                case "DROPDOWN":
                    if (!field.containsKey("options") || field.get("options") == null || ((List<?>) field.get("options")).isEmpty()) {
                        context.disableDefaultConstraintViolation();
                        context.buildConstraintViolationWithTemplate("Field at index " + i + ": options are required for dropdown type")
                                .addConstraintViolation();
                        return false;
                    }
                    break;

            }
        }
        return true;
    }
}
