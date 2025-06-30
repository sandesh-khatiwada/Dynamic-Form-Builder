package com.sandesh.formbuilder.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandesh.formbuilder.dto.FormDataRequest;
import com.sandesh.formbuilder.dto.FormDataResponse;
import com.sandesh.formbuilder.dto.FormRequest;
import com.sandesh.formbuilder.dto.FormResponse;
import com.sandesh.formbuilder.entity.FormData;
import com.sandesh.formbuilder.entity.FormTemplate;
import com.sandesh.formbuilder.repository.FormDataRepository;
import com.sandesh.formbuilder.repository.FormRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.stereotype.Service;



import java.util.*;

@Service
@RequiredArgsConstructor
public class FormServiceImpl implements FormService{

    private final FormRepository formRepository;
    private final FormDataRepository formDataRepository;
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

    public FormDataResponse fillUpForm(FormDataRequest formDataRequest, UUID formId) {

        List<Map<String, Object>> jsonData = formDataRequest.getJsonData();

        if(formId==null){
            throw new IllegalArgumentException("Form Template ID is required");
        }

        // Retrieve the form template
        FormTemplate formTemplate = formRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form Template with id: " + formId + " does not exist."));

        // Get and validate the JSON schema (field definitions)
        String jsonSchemaString = formTemplate.getJsonSchema();
        if (jsonSchemaString == null || jsonSchemaString.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON schema is null or empty for form template with id: " + formId);
        }

        try {
            // Parse the schema as a JSONArray
            JSONArray schemaArray = new JSONArray(jsonSchemaString);
            JSONArray dataArray = new JSONArray(jsonData);

            // Check if the number of items matches
            if (schemaArray.length() != dataArray.length()) {
                throw new IllegalArgumentException("JSON data length (" + dataArray.length() + ") does not match schema length (" + schemaArray.length() + ")");
            }

            // Validate each item in jsonData against the corresponding schema item
            for (int i = 0; i < schemaArray.length(); i++) {
                JSONObject schemaItem = schemaArray.getJSONObject(i);
                JSONObject dataItem = dataArray.getJSONObject(i);

                // Check required fields
                if (!dataItem.has("label") || !dataItem.has("type") || !dataItem.has("key") || !dataItem.has("value")) {
                    throw new IllegalArgumentException("JSON data at index " + i + " missing required fields (label, type, key, value)");
                }

                // Check field values
                if (!schemaItem.getString("label").equals(dataItem.getString("label"))) {
                    throw new IllegalArgumentException("JSON data at index " + i + ": label mismatch, expected '" + schemaItem.getString("label") + "', got '" + dataItem.getString("label") + "'");
                }
                if (!schemaItem.getString("type").equals(dataItem.getString("type"))) {
                    throw new IllegalArgumentException("JSON data at index " + i + ": type mismatch, expected '" + schemaItem.getString("type") + "', got '" + dataItem.getString("type") + "'");
                }
                if (!schemaItem.getString("key").equals(dataItem.getString("key"))) {
                    throw new IllegalArgumentException("JSON data at index " + i + ": key mismatch, expected '" + schemaItem.getString("key") + "', got '" + dataItem.getString("key") + "'");
                }

                // Check required field
                if (schemaItem.has("required") && schemaItem.getBoolean("required") && dataItem.isNull("value")) {
                    throw new IllegalArgumentException("JSON data at index " + i + ": value is required for key '" + schemaItem.getString("key") + "'");
                }

                // Validate value based on type
                String type = schemaItem.getString("type");
                Object value = dataItem.get("value");
                switch (type) {
                    case "text":
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException("JSON data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a string");
                        }
                        break;
                    case "number":
                        if (!(value instanceof Number)) {
                            throw new IllegalArgumentException("JSON data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a number");
                        }
                        break;
                    case "dropdown":
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException("JSON data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a string");
                        }
                        if (schemaItem.has("options")) {
                            JSONArray options = schemaItem.getJSONArray("options");
                            Set<String> validOptions = new HashSet<>();
                            for (int j = 0; j < options.length(); j++) {
                                validOptions.add(options.getString(j));
                            }
                            if (!validOptions.contains(value)) {
                                throw new IllegalArgumentException("JSON data at index " + i + ": value '" + value + "' for key '" + schemaItem.getString("key") + "' is not a valid option");
                            }
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("JSON schema at index " + i + ": unsupported type '" + type + "'");
                }

                // Check options field if present
                if (schemaItem.has("options") && dataItem.has("options")) {
                    JSONArray schemaOptions = schemaItem.getJSONArray("options");
                    JSONArray dataOptions = dataItem.getJSONArray("options");
                    if (schemaOptions.length() != dataOptions.length()) {
                        throw new IllegalArgumentException("JSON data at index " + i + ": options length mismatch for key '" + schemaItem.getString("key") + "'");
                    }
                    for (int j = 0; j < schemaOptions.length(); j++) {
                        if (!schemaOptions.getString(j).equals(dataOptions.getString(j))) {
                            throw new IllegalArgumentException("JSON data at index " + i + ": options mismatch for key '" + schemaItem.getString("key") + "'");
                        }
                    }
                }
            }


            FormData formData = new FormData();
            formData.setJsonData(dataArray.toString());
            formData.setFormTemplate(formTemplate);


            formDataRepository.save(formData);

            //  add to formTemplate's formDataList
            formTemplate.getFormDataList().add(formData);
            formRepository.save(formTemplate);

            FormDataResponse formDataResponse = new FormDataResponse();
            formDataResponse.setId(formId);
            formDataResponse.setJsonData(formDataRequest.getJsonData());

            return formDataResponse;
        } catch (org.json.JSONException e) {
            throw new IllegalArgumentException("Invalid JSON schema for form template with id: " + formId + ": " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing form data: " + e.getMessage());
        }
    }

    @Override
    @Transactional

    public List<FormDataResponse> getFormDataByTemplateId(UUID templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template Id is required");
        }

        List<FormData> formData = formDataRepository.findByFormTemplateId(templateId);
        List<FormDataResponse> formDataResponses = new ArrayList<>();

        for (FormData data : formData) {
            FormDataResponse formDataResponse = new FormDataResponse();
            formDataResponse.setId(data.getId());

            // Convert JSON string to List<Map<String, Object>>
            List<Map<String, Object>> jsonDataList = new ArrayList<>();
            try {
                if (data.getJsonData() != null && !data.getJsonData().trim().isEmpty()) {
                    JSONArray jsonArray = new JSONArray(data.getJsonData());
                    for (int i = 0; i < jsonArray.length(); i++) {
                        JSONObject jsonObject = jsonArray.getJSONObject(i);
                        Map<String, Object> map = new HashMap<>();
                        for (String key : jsonObject.keySet()) {
                            map.put(key, jsonObject.get(key));
                        }
                        jsonDataList.add(map);
                    }
                }
                formDataResponse.setJsonData(jsonDataList);
            } catch (org.json.JSONException e) {
                throw new IllegalArgumentException("Error parsing JSON data for FormData with id: " + data.getId() + ": " + e.getMessage());
            }

            formDataResponses.add(formDataResponse);
        }

        return formDataResponses;
    }

    @Override
    public FormResponse getFormTemplateById(UUID templateId){

        if(templateId==null){
            throw new IllegalArgumentException("Template ID is required");
        }

        FormTemplate formTemplate = formRepository.findById(templateId).orElseThrow(()->new IllegalArgumentException("Form Template with template ID : "+templateId+" does not exist."));

        try {
            FormResponse formResponse = new FormResponse();
            formResponse.setId(formTemplate.getId());
            formResponse.setName(formTemplate.getName());

            List<Map<String, Object>> parsedSchema = objectMapper.readValue(formTemplate.getJsonSchema(), new TypeReference<List<Map<String, Object>>>() {
            });


            formResponse.setJsonSchema(parsedSchema);

            return formResponse;

        }catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse JSON schema for form, " +formTemplate.getId()+ ", "+ e.getMessage());

        }

    }


    @Override
    public void deleteFormTemplateById(UUID templateId){

        if(templateId==null){
            throw new IllegalArgumentException("Template ID is required");
        }

        if(formRepository.findById(templateId).isPresent()) {
            formRepository.deleteById(templateId);
        }else{
            throw new IllegalArgumentException("Form Template with Template ID: "+templateId+" does not exist. ");
        }

    }

    @Override
    public void deleteFormDataById(UUID formId){
        if(formId==null){
            throw new IllegalArgumentException("Form ID is required");
        }

        if(formDataRepository.findById(formId).isPresent()) {
            formDataRepository.deleteById(formId);
        }else{
            throw new IllegalArgumentException("Form Data with ID: "+formId+" does not exist. ");
        }

    }


}


