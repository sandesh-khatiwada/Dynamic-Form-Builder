package com.sandesh.formbuilder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandesh.formbuilder.dto.FormRequest;
import com.sandesh.formbuilder.dto.FormResponse;
import com.sandesh.formbuilder.entity.FormTemplate;
import com.sandesh.formbuilder.repository.FormRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class FormServiceImpl implements FormService{

    private final FormRepository formRepository;
    private final ObjectMapper objectMapper;


    @Override
    public FormResponse createForm(FormRequest formCreationRequest) {
        try {

            // Serialize jsonSchema to string for storage
            String jsonSchemaString = objectMapper.writeValueAsString(formCreationRequest.getJsonSchema());

            FormTemplate formTemplate = new FormTemplate();
            formTemplate.setName(formCreationRequest.getName());
            formTemplate.setJsonSchema(jsonSchemaString);
            FormTemplate form = formRepository.save(formTemplate);

            FormResponse formCreationResponse = new FormResponse();
            formCreationResponse.setId(form.getId());
            formCreationResponse.setName(form.getName());
            formCreationResponse.setJsonSchema(formCreationRequest.getJsonSchema());

            return formCreationResponse;

        }catch (JsonProcessingException exception){
            throw new IllegalArgumentException("Invalid JSON schema: " + exception.getMessage());

        }
    }

    public List<FormResponse> getAllForms(){

            List<FormTemplate> forms = formRepository.findAll();
            List<FormResponse> formResponses = new ArrayList<>();
            forms.forEach((formTemplate -> {

                try {
                    FormResponse formResponse = new FormResponse();
                    formResponse.setId(formTemplate.getId());
                    formResponse.setName(formTemplate.getName());

                    List<Map<String, Object>> parsedSchema = objectMapper.readValue(formTemplate.getJsonSchema(), new TypeReference<List<Map<String, Object>>>() {
                    });


                    formResponse.setJsonSchema(parsedSchema);
                    formResponses.add(formResponse);
                } catch (JsonProcessingException e) {
                    throw new IllegalArgumentException("Failed to parse JSON schema for form, " +formTemplate.getId()+ ", "+ e.getMessage());

                }
            }));


            return formResponses;

        }
    }


