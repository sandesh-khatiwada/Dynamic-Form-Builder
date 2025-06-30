package com.sandesh.formbuilder.service;

import com.sandesh.formbuilder.dto.FormDataRequest;
import com.sandesh.formbuilder.dto.FormDataResponse;
import com.sandesh.formbuilder.dto.FormRequest;

import com.sandesh.formbuilder.dto.FormResponse;

import java.util.List;
import java.util.UUID;

public interface FormService {

    FormResponse createForm(FormRequest formCreationRequest);
    List<FormResponse> getAllForms();
    FormDataResponse fillUpForm(FormDataRequest formDataRequest);
    List<FormDataResponse> getFormDataByTemplateId(UUID templateId);

    }
