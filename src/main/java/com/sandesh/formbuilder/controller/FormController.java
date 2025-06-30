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


    @GetMapping("/forms/{id}")
    public ResponseEntity<APIResponse<FormResponse>> getAllForms(@PathVariable UUID id){
        FormResponse form= formService.getFormTemplateById(id);


        APIResponse<FormResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form retrieved succesfully",
                form
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }


    @PostMapping("/forms/{id}/data")
    public ResponseEntity<APIResponse<FormDataResponse>> fillUpForm(@RequestBody FormDataRequest formDataRequest, @PathVariable UUID id){

        FormDataResponse formDataResponse = formService.fillUpForm(formDataRequest, id);


        APIResponse<FormDataResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form filled up succesfully",
                formDataResponse
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }

    @GetMapping("/forms/{id}/data")
    public ResponseEntity<APIResponse<List<FormDataResponse>>> getFormDataByTemplateId(@PathVariable UUID id){
        List<FormDataResponse> formDataResponses = formService.getFormDataByTemplateId(id);

        APIResponse<List<FormDataResponse>> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form data retrieved succesfully",
                formDataResponses
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }



    @DeleteMapping("/forms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Object>> deleteFormTemplateByTemplateId(@PathVariable UUID id){

        formService.deleteFormTemplateById(id);

        APIResponse<Object> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form Template deleted succesfully"
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @DeleteMapping("/forms/{id}/data")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Object>> deleteFormDataById(@PathVariable UUID id){

        formService.deleteFormDataById(id);

        APIResponse<Object> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form Data deleted succesfully"
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }




}
