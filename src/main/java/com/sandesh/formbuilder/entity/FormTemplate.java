package com.sandesh.formbuilder.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


import java.util.UUID;

@Data
@Entity
@AllArgsConstructor
@NoArgsConstructor
public class FormTemplate {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private String name;

    @Lob
    private String jsonSchema;


}
