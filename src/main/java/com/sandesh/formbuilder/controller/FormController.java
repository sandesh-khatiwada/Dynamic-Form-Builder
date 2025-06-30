package com.sandesh.formbuilder.controller;

import com.sandesh.formbuilder.dto.FormDataRequest;
import com.sandesh.formbuilder.dto.FormDataResponse;
import com.sandesh.formbuilder.dto.FormRequest;
import com.sandesh.formbuilder.dto.FormResponse;
import com.sandesh.formbuilder.service.FormService;
import com.sandesh.formbuilder.util.APIResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FormController {

    private final FormService formService;

    @PostMapping("/forms")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<FormResponse>> createForm(@RequestBody @Valid FormRequest formCreationRequest){

        FormResponse formCreationResponse = formService.createForm(formCreationRequest);

        APIResponse<FormResponse> apiResponse = new APIResponse<>(
                HttpStatus.CREATED,
                "Form created Succesfully",
                formCreationResponse
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

    @GetMapping("/forms")
    public ResponseEntity<APIResponse<List<FormResponse>>> getAllForms(){
         List<FormResponse> forms= formService.getAllForms();


        APIResponse<List<FormResponse>> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Forms retrieved succesfully",
                forms
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }

    @PostMapping("/forms/data")
    public ResponseEntity<APIResponse<FormDataResponse>> fillUpForm(@RequestBody FormDataRequest formDataRequest){

        FormDataResponse formDataResponse = formService.fillUpForm(formDataRequest);


        APIResponse<FormDataResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form filled up succesfully",
                formDataResponse
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }

    @GetMapping("/forms/{id}")
    public ResponseEntity<APIResponse<List<FormDataResponse>>> getFormDataByTemplateId(@PathVariable UUID id){
        List<FormDataResponse> formDataResponses = formService.getFormDataByTemplateId(id);

        APIResponse<List<FormDataResponse>> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form data retrieved succesfully",
                formDataResponses
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }



}
