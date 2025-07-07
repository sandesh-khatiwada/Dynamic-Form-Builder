package com.sandesh.formbuilder.service.form;

import com.sandesh.formbuilder.dto.FormDataRequest;
import com.sandesh.formbuilder.dto.FormDataResponse;
import com.sandesh.formbuilder.dto.FormRequest;

import com.sandesh.formbuilder.dto.FormResponse;
import jakarta.servlet.http.HttpServletResponse;

import java.util.List;
import java.util.UUID;

public interface FormService {
    FormResponse createForm(FormRequest formCreationRequest);
    List<FormResponse> getAllForms(int offset, int limit, String name);
    FormDataResponse fillUpForm(FormDataRequest formDataRequest, UUID formTemplateId, boolean provideResponse);
    List<FormDataResponse> getFormDataByTemplateId(UUID templateId, int offset, int limit);
    List<FormDataResponse> getFormResponseByTemplateId(UUID id);
    FormResponse getFormTemplateById(UUID templateId);
    void deleteFormTemplateById(UUID templateId);
    void deleteFormDataById(UUID formId);
    FormDataResponse editFormDataById(UUID id,FormDataRequest newFormData);
    FormDataResponse getFormDataById(UUID id);
    void exportFormDataToExcel(UUID templateId, HttpServletResponse response);
}
