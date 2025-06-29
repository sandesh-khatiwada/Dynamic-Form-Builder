package com.sandesh.formbuilder.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = JsonSchemaValidator.class)
@Target({ ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidJsonSchema {
    String message() default "Invalid JSON schema: each field must have label, type, and key, with valid type values";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}