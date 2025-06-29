package com.sandesh.formbuilder.service;

import com.sandesh.formbuilder.dto.FormRequest;

import com.sandesh.formbuilder.dto.FormResponse;

import java.util.List;

public interface FormService {

    FormResponse createForm(FormRequest formCreationRequest);
    List<FormResponse> getAllForms();
}
