package com.sandesh.formbuilder.service.form;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sandesh.formbuilder.dto.FormDataRequest;
import com.sandesh.formbuilder.dto.FormDataResponse;
import com.sandesh.formbuilder.dto.FormRequest;
import com.sandesh.formbuilder.dto.FormResponse;
import com.sandesh.formbuilder.entity.FormData;
import com.sandesh.formbuilder.entity.FormTemplate;
import com.sandesh.formbuilder.entity.User;
import com.sandesh.formbuilder.exception.FormExportException;
import com.sandesh.formbuilder.repository.FormDataRepository;
import com.sandesh.formbuilder.repository.FormRepository;
import com.sandesh.formbuilder.repository.UserRepository;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Service
@RequiredArgsConstructor
public class FormServiceImpl implements FormService {

    private final FormRepository formRepository;
    private final FormDataRepository formDataRepository;
    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;
    private final JavaMailSender mailSender;

    @Override
    public FormResponse createForm(FormRequest formCreationRequest) {
        try {
            // Serialize jsonSchema to string for storage
            String jsonSchemaString = objectMapper.writeValueAsString(formCreationRequest.getJsonSchema());

            FormTemplate formTemplate = new FormTemplate();
            formTemplate.setName(formCreationRequest.getName());
            formTemplate.setJsonSchema(jsonSchemaString);
            formTemplate.setAllowEdit(formCreationRequest.isAllowEdit());
            formTemplate.setAllowDelete(formCreationRequest.isAllowDelete());
            FormTemplate form = formRepository.save(formTemplate);

            FormResponse formCreationResponse = new FormResponse();
            formCreationResponse.setTemplateId(form.getId());
            formCreationResponse.setName(form.getName());
            formCreationResponse.setCreatedAt(form.getCreatedAt());
            formCreationResponse.setJsonSchema(formCreationRequest.getJsonSchema());
            formCreationResponse.setAllowEdit(form.isAllowEdit());
            formCreationResponse.setAllowDelete(form.isAllowDelete());
            return formCreationResponse;
        } catch (JsonProcessingException exception) {
            throw new IllegalArgumentException("Invalid JSON schema: " + exception.getMessage());
        }
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormResponse> getAllForms(int offset, int limit, String name) {
        List<FormTemplate> forms;
        if (name != null && !name.trim().isEmpty()) {
            forms = formRepository.findByNameWithOffsetAndLimit(name.trim(), offset, limit);
        } else {
            forms = formRepository.findAllWithOffsetAndLimit(offset, limit);
        }
        List<FormResponse> formResponses = new ArrayList<>();
        for (FormTemplate formTemplate : forms) {
            try {
                FormResponse formResponse = new FormResponse();
                formResponse.setTemplateId(formTemplate.getId());
                formResponse.setName(formTemplate.getName());
                formResponse.setCreatedAt(formTemplate.getCreatedAt());
                formResponse.setAllowEdit(formTemplate.isAllowEdit());
                formResponse.setAllowDelete(formTemplate.isAllowDelete());
                List<Map<String, Object>> parsedSchema = objectMapper.readValue(formTemplate.getJsonSchema(), new TypeReference<List<Map<String, Object>>>() {});
                formResponse.setJsonSchema(parsedSchema);
                formResponses.add(formResponse);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Failed to parse JSON schema for form " + formTemplate.getId() + ": " + e.getMessage());
            }
        }
        return formResponses;
    }


    @Override
    public FormDataResponse fillUpForm(FormDataRequest formDataRequest, UUID formId, boolean provideResponse) {
        if (formId == null) {
            throw new IllegalArgumentException("Form Template ID is required");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        // Extract email from JWT token
        String email = extractEmailFromToken(authentication);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));

        // Retrieve the form template
        FormTemplate formTemplate = formRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form Template with id: " + formId + " does not exist."));

        try {
            FormData formData = validateFormData(formTemplate, formDataRequest.getJsonData());
            formData.setFormTemplate(formTemplate);
            formData.setUser(user);

            FormData savedFormData = formDataRepository.save(formData);

            // Add to formTemplate's formDataList
            formTemplate.getFormDataList().add(formData);
            formRepository.save(formTemplate);

            FormDataResponse formDataResponse = new FormDataResponse();
            formDataResponse.setFormDataId(savedFormData.getId());
            formDataResponse.setJsonData(formDataRequest.getJsonData());
            formDataResponse.setCreatedAt(savedFormData.getCreatedAt());

            // Send email if provideResponse is true
            if (provideResponse) {
                sendResponseEmail(email, formDataRequest.getJsonData(), formTemplate.getName());
            }

            return formDataResponse;
        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid schema for form template with id: " + formId + ": " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error parsing form data: " + e.getMessage());
        }
    }



    @Override
    @Transactional(readOnly = true)
    public List<FormDataResponse> getFormDataByTemplateId(UUID templateId, int offset, int limit) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template Id is required");
        }

        List<FormData> formData = formDataRepository.findByFormTemplateIdWithOffsetAndLimit(templateId, offset, limit);
        List<FormDataResponse> formDataResponses = new ArrayList<>();

        for (FormData data : formData) {
            FormDataResponse formDataResponse = new FormDataResponse();
            formDataResponse.setFormDataId(data.getId());
            formDataResponse.setCreatedAt(data.getCreatedAt());

            // Convert JSON string to List<Map<String, Object>>
            List<Map<String, Object>> jsonDataList = new ArrayList<>();
            try {
                if (data.getJsonData() != null && !data.getJsonData().trim().isEmpty()) {
                    // Parse the stored JSON string using ObjectMapper to handle any format
                    jsonDataList = objectMapper.readValue(data.getJsonData(), new TypeReference<List<Map<String, Object>>>() {});
                }
                formDataResponse.setJsonData(jsonDataList);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error parsing FormData with id: " + data.getId() + ": " + e.getMessage());
            }

            formDataResponses.add(formDataResponse);
        }

        return formDataResponses;
    }

    @Override
    @Transactional(readOnly = true)
    public List<FormDataResponse> getFormResponseByTemplateId(UUID templateId){
        if (templateId == null) {
            throw new IllegalArgumentException("Template Id is required");
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        // Extract email from JWT token
        String email = extractEmailFromToken(authentication);
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalStateException("User not found"));


        //Get form data with templateId and user
        List<FormData> formData = formDataRepository.findByFormTemplateIdAndUserId(templateId, user.getId());


        List<FormDataResponse> formDataResponses = new ArrayList<>();

        for (FormData data : formData) {
            FormDataResponse formDataResponse = new FormDataResponse();
            formDataResponse.setFormDataId(data.getId());
            formDataResponse.setCreatedAt(data.getCreatedAt());

            // Convert JSON string to List<Map<String, Object>>
            List<Map<String, Object>> jsonDataList = new ArrayList<>();
            try {
                if (data.getJsonData() != null && !data.getJsonData().trim().isEmpty()) {
                    // Parse the stored JSON string using ObjectMapper to handle any format
                    jsonDataList = objectMapper.readValue(data.getJsonData(), new TypeReference<List<Map<String, Object>>>() {});
                }
                formDataResponse.setJsonData(jsonDataList);
            } catch (JsonProcessingException e) {
                throw new IllegalArgumentException("Error parsing FormData with id: " + data.getId() + ": " + e.getMessage());
            }

            formDataResponses.add(formDataResponse);
        }

        return formDataResponses;
    }


    @Override
    public FormResponse getFormTemplateById(UUID templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template ID is required");
        }

        FormTemplate formTemplate = formRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Form Template with template ID: " + templateId + " does not exist."));

        try {
            FormResponse formResponse = new FormResponse();
            formResponse.setTemplateId(formTemplate.getId());
            formResponse.setName(formTemplate.getName());
            formResponse.setCreatedAt(formTemplate.getCreatedAt());
            formResponse.setAllowEdit(formTemplate.isAllowEdit());
            formResponse.setAllowDelete(formTemplate.isAllowDelete());


            List<Map<String, Object>> parsedSchema = objectMapper.readValue(formTemplate.getJsonSchema(), new TypeReference<List<Map<String, Object>>>() {});
            formResponse.setJsonSchema(parsedSchema);

            return formResponse;
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to parse schema for form " + formTemplate.getId() + ": " + e.getMessage());
        }
    }

    @Override
    public void deleteFormTemplateById(UUID templateId) {
        if (templateId == null) {
            throw new IllegalArgumentException("Template ID is required");
        }

        if (formRepository.findById(templateId).isPresent()) {
            formRepository.deleteById(templateId);
        } else {
            throw new IllegalArgumentException("Form Template with Template ID: " + templateId + " does not exist.");
        }
    }

    @Override
    public void deleteFormDataById(UUID formId) {
        if (formId == null) {
            throw new IllegalArgumentException("Form ID is required");
        }

        FormData formData = formDataRepository.findById(formId)
                .orElseThrow(() -> new IllegalArgumentException("Form data with provided id does not exist"));

        String userEmail = formData.getUser().getEmail();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        if (!Objects.equals(email, userEmail)) {
            throw new AccessDeniedException("You don't have access to delete this form.");
        }

        formDataRepository.deleteById(formId);
    }

    @Override
    @Transactional
    public FormDataResponse editFormDataById(UUID id, FormDataRequest newFormData) {
        if (id == null) {
            throw new IllegalArgumentException("Form Data ID is required");
        }

        // Retrieve the existing FormData entity
        FormData existingFormData = formDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Form Data ID is invalid"));


        if(!existingFormData.getFormTemplate().isAllowEdit()){
            throw new AccessDeniedException("You can not edit this form data");
        }



        //Only user who has created the form data can edit it
        String userEmail = existingFormData.getUser().getEmail();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        if (!Objects.equals(email, userEmail)) {
            throw new AccessDeniedException("You do not have access to edit this form data.");
        }


        // Validate the new form data against the template
        FormTemplate formTemplate = existingFormData.getFormTemplate();
        validateFormData(formTemplate, newFormData.getJsonData());

        // Update the existing FormData with the new jsonData
        try {
            existingFormData.setJsonData(objectMapper.writeValueAsString(newFormData.getJsonData()));
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Failed to serialize jsonData: " + e.getMessage());
        }

        // Save the updated entity
        FormData savedFormData = formDataRepository.save(existingFormData);

        // Prepare the response
        FormDataResponse formDataResponse = new FormDataResponse();
        formDataResponse.setFormDataId(savedFormData.getId());
        formDataResponse.setJsonData(newFormData.getJsonData());
        formDataResponse.setCreatedAt(savedFormData.getCreatedAt());

        return formDataResponse;
    }

    @Override
    public FormDataResponse getFormDataById(UUID id){
        if(id==null){
            throw  new IllegalArgumentException("Form Data Id is required");
        }


        FormData formData = formDataRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Form data with provided id does not exist"));



        //Only user who has created the form data can access it
        String userEmail = formData.getUser().getEmail();

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("User not authenticated");
        }

        String email = ((UserDetails) authentication.getPrincipal()).getUsername();

        if (!Objects.equals(email, userEmail)) {
            throw new AccessDeniedException("You can not access this form data.");
        }

        try {
            FormDataResponse formDataResponse = new FormDataResponse();
            formDataResponse.setFormDataId(formData.getId());
            formDataResponse.setCreatedAt(formData.getCreatedAt());

            List<Map<String, Object>> jsonDataList = new ArrayList<>();
            jsonDataList = objectMapper.readValue(formData.getJsonData(), new TypeReference<List<Map<String, Object>>>() {
            });


            formDataResponse.setJsonData(jsonDataList);
            return formDataResponse;

        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error parsing FormData with id: " + formData.getId() + ": " + e.getMessage());
        }

    }

    @Transactional(readOnly = true)
    public void exportFormDataToExcel(UUID templateId, HttpServletResponse response) {

        FormTemplate formTemplate = formRepository.findById(templateId)
                .orElseThrow(() -> new IllegalArgumentException("Form Template with ID " + templateId + " not found"));

        // Fetch all FormData for the template
        List<FormData> formDataList = formDataRepository.findByFormTemplateId(templateId);
        if (formDataList.isEmpty()) {
            throw new IllegalArgumentException("No form data found for template ID " + templateId);
        }

        // Create a streaming workbook for large datasets
        try (SXSSFWorkbook workbook = new SXSSFWorkbook()) {
            // Create a sheet
            Sheet sheet = workbook.createSheet(formTemplate.getName() + "Responses");

            // Create header row
            Row headerRow = sheet.createRow(0);
            List<Map<String, Object>> schema = objectMapper.readValue(formTemplate.getJsonSchema(), new TypeReference<List<Map<String, Object>>>() {});
            int columnIndex = 0;
            for (Map<String, Object> field : schema) {
                Cell cell = headerRow.createCell(columnIndex++);
                cell.setCellValue((String) field.get("label")); // label is used as column header
            }
            headerRow.createCell(columnIndex).setCellValue("Submitted At");

            // Populate data rows
            int rowNum = 1;
            for (FormData formData : formDataList) {
                Row row = sheet.createRow(rowNum++);
                List<Map<String, Object>> jsonData = objectMapper.readValue(formData.getJsonData(), new TypeReference<List<Map<String, Object>>>() {});

                columnIndex = 0;
                for (Map<String, Object> data : jsonData) {
                    Cell cell = row.createCell(columnIndex++);
                    cell.setCellValue(data.get("value") != null ? data.get("value").toString() : "");
                }
                // Add createdAt
                Cell createdAtCell = row.createCell(columnIndex);
                createdAtCell.setCellValue(formData.getCreatedAt() != null ? formData.getCreatedAt().toString() : "");
            }

            // Set response headers
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String filename = "form_responses_" + templateId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + filename);

            // Write the workbook to the response output stream
            workbook.write(response.getOutputStream());
            workbook.dispose(); // Clean up temporary files
        } catch (IOException e) {
            throw new FormExportException("Error creating Excel workbook");
        }
    }

    private FormData validateFormData(FormTemplate formTemplate, List<Map<String, Object>> jsonData) {
        // Get and validate the JSON schema (field definitions)
        String jsonSchemaString = formTemplate.getJsonSchema();
        if (jsonSchemaString == null || jsonSchemaString.trim().isEmpty()) {
            throw new IllegalArgumentException("JSON schema is null or empty for form template with id: " + formTemplate.getId());
        }

        try {
            // Parse the schema as a JSONArray
            JSONArray schemaArray = new JSONArray(jsonSchemaString);
            JSONArray dataArray = new JSONArray(objectMapper.writeValueAsString(jsonData)); // Ensure proper JSON array

            // Check if the number of items matches
            if (schemaArray.length() != dataArray.length()) {
                throw new IllegalArgumentException("Form data length (" + dataArray.length() + ") does not match template length (" + schemaArray.length() + ")");
            }

            // Validate each item in jsonData against the corresponding schema item
            FormData formData = new FormData();
            for (int i = 0; i < schemaArray.length(); i++) {
                JSONObject schemaItem = schemaArray.getJSONObject(i);
                JSONObject dataItem = dataArray.getJSONObject(i);

                // Check required fields
                if (!dataItem.has("label") || !dataItem.has("type") || !dataItem.has("key") || !dataItem.has("value")) {
                    throw new IllegalArgumentException("Data at index " + i + " missing required fields (label, type, key, value)");
                }

                // Check field values
                if (!schemaItem.getString("label").equals(dataItem.getString("label"))) {
                    throw new IllegalArgumentException("Data at index " + i + ": label mismatch, expected '" + schemaItem.getString("label") + "', got '" + dataItem.getString("label") + "'");
                }
                if (!schemaItem.getString("type").equals(dataItem.getString("type"))) {
                    throw new IllegalArgumentException("Data at index " + i + ": type mismatch, expected '" + schemaItem.getString("type") + "', got '" + dataItem.getString("type") + "'");
                }
                if (!schemaItem.getString("key").equals(dataItem.getString("key"))) {
                    throw new IllegalArgumentException("Data at index " + i + ": key mismatch, expected '" + schemaItem.getString("key") + "', got '" + dataItem.getString("key") + "'");
                }

                // Check required field
                if (schemaItem.has("required") && schemaItem.getBoolean("required") && dataItem.isNull("value")) {
                    throw new IllegalArgumentException("Data at index " + i + ": value is required for key '" + schemaItem.getString("key") + "'");
                }

                // Validate value based on type
                String type = schemaItem.getString("type").toLowerCase();
                boolean required = schemaItem.getBoolean("required");
                Object value = dataItem.get("value");
                switch (type) {
                    case "text":
                    case "textarea":
                        if (required && !(value instanceof String)) {
                            throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a string");
                        }
                        break;
                    case "number":
                        if (required && !(value instanceof Number)) {
                            throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a number");
                        }
                        break;
                    case "dropdown":
                        if ( required && !(value instanceof String)) {
                            throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a string");
                        }
                        if (schemaItem.has("options")) {
                            JSONArray options = schemaItem.getJSONArray("options");
                            Set<String> validOptions = new HashSet<>();
                            for (int j = 0; j < options.length(); j++) {
                                validOptions.add(options.getString(j));
                            }
                            if (!validOptions.contains(value)) {
                                throw new IllegalArgumentException("Data at index " + i + ": value '" + value + "' for key '" + schemaItem.getString("key") + "' is not a valid option");
                            }
                        }
                        break;
                    case "date":
                        if (value != null) {
                            try {
                                LocalDate.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE);
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a valid date (YYYY-MM-DD)");
                            }
                        }
                        break;
                    case "time":
                        if (value != null) {
                            try {
                                LocalTime.parse((String) value, DateTimeFormatter.ISO_LOCAL_TIME);
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a valid time (HH:MM:SS)");
                            }
                        }
                        break;
                    case "datetime":
                        if (value != null) {
                            try {
                                LocalDateTime.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                            } catch (Exception e) {
                                throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a valid datetime (YYYY-MM-DDTHH:MM:SS)");
                            }
                        }
                        break;
                    case "email":
                        if (!(value instanceof String)) {
                            throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a string");
                        }
                        if (value != null && !((String) value).matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
                            throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a valid email address");
                        }
                        break;
                    case "checkbox":
                        if (value != null && !(value instanceof Boolean)) {
                            throw new IllegalArgumentException("Data at index " + i + ": value for key '" + schemaItem.getString("key") + "' must be a boolean");
                        }
                        break;
                    default:
                        throw new IllegalArgumentException("Schema at index " + i + ": unsupported type '" + type + "'");
                }

                // Check options field if present
                if (schemaItem.has("options") && dataItem.has("options")) {
                    JSONArray schemaOptions = schemaItem.getJSONArray("options");
                    JSONArray dataOptions = dataItem.getJSONArray("options");
                    if (schemaOptions.length() != dataOptions.length()) {
                        throw new IllegalArgumentException("Data at index " + i + ": options length mismatch for key '" + schemaItem.getString("key") + "'");
                    }
                    for (int j = 0; j < schemaOptions.length(); j++) {
                        if (!schemaOptions.getString(j).equals(dataOptions.getString(j))) {
                            throw new IllegalArgumentException("Data at index " + i + ": options mismatch for key '" + schemaItem.getString("key") + "'");
                        }
                    }
                }
            }

            // Store as a properly formatted JSON string
            formData.setJsonData(objectMapper.writeValueAsString(jsonData));
            return formData;

        } catch (JSONException e) {
            throw new IllegalArgumentException("Invalid JSON schema for form template with id: " + formTemplate.getId() + ": " + e.getMessage());
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Error serializing form data: " + e.getMessage());
        } catch (Exception e) {
            throw new IllegalArgumentException("Error processing form data: " + e.getMessage());
        }
    }

    private String extractEmailFromToken(Authentication authentication) {
        Object principal = authentication.getPrincipal();
        if (principal instanceof UserDetails) {
            String email = ((UserDetails) principal).getUsername();
            return email;
        }
        throw new IllegalStateException("Unable to extract email from token");
    }

    private void sendResponseEmail(String to, List<Map<String, Object>> formData, String formName) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Form Response Submission - " + formName);
        message.setText(buildEmailBody(formData, formName));
        mailSender.send(message);
    }


    private String buildEmailBody(List<Map<String, Object>> formData, String formName) {
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter datetimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");


        StringBuilder body = new StringBuilder();
        body.append("Thank you for submitting the form: ").append(formName).append("\n\n");
        body.append("Your responses:\n");
        for (Map<String, Object> field : formData) {
            String label = (String) field.get("label");
            Object value = field.get("value");

            if(field.get("type").equals("date")){
                if (value instanceof String) {
                    LocalDate date = LocalDate.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE);
                    value = date.format(dateFormatter);
                }
            }

            else if(field.get("type").equals("datetime")){
                if (value instanceof String) {
                    LocalDateTime datetime = LocalDateTime.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    value = datetime.format(datetimeFormatter);
                }
            }


            body.append(label).append(": ").append(value != null ? value.toString() : "N/A").append("\n");
        }
        body.append("\nSubmitted on: ").append(LocalDateTime.now().format(datetimeFormatter));
        return body.toString();
    }


}