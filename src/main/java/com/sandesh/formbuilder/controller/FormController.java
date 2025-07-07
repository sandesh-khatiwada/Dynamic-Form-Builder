package com.sandesh.formbuilder.controller;

import com.sandesh.formbuilder.dto.FormDataRequest;
import com.sandesh.formbuilder.dto.FormDataResponse;
import com.sandesh.formbuilder.dto.FormRequest;
import com.sandesh.formbuilder.dto.FormResponse;
import com.sandesh.formbuilder.service.form.FormService;
import com.sandesh.formbuilder.util.APIResponse;
import jakarta.servlet.http.HttpServletResponse;
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
                "Form created successfully",
                formCreationResponse
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.CREATED);
    }

    @GetMapping("/forms")
    public ResponseEntity<APIResponse<List<FormResponse>>> getAllForms(
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit,
            @RequestParam(required = false) String name) {

        List<FormResponse> forms = formService.getAllForms(offset, limit, name);

        APIResponse<List<FormResponse>> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Forms retrieved successfully",
                forms
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/forms/{id}")
    public ResponseEntity<APIResponse<FormResponse>> getFormtemplateById(@PathVariable UUID id){
        FormResponse form= formService.getFormTemplateById(id);


        APIResponse<FormResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form retrieved successfully",
                form
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }


    @PostMapping("/forms/{id}/data")
    public ResponseEntity<APIResponse<FormDataResponse>> fillUpForm(
            @RequestBody FormDataRequest formDataRequest,
            @PathVariable UUID id,
            @RequestParam(defaultValue = "false") boolean provideResponse) {

        FormDataResponse formDataResponse = formService.fillUpForm(formDataRequest, id, provideResponse);

        APIResponse<FormDataResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form filled up successfully",
                formDataResponse
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }

    @GetMapping("/forms/{id}/data")
    public ResponseEntity<APIResponse<List<FormDataResponse>>> getFormDataByTemplateId(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "0") int offset,
            @RequestParam(defaultValue = "10") int limit) {

        List<FormDataResponse> formDataResponses = formService.getFormDataByTemplateId(id, offset, limit);

        APIResponse<List<FormDataResponse>> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form data retrieved successfully",
                formDataResponses
        );

        return new ResponseEntity<>(apiResponse, HttpStatus.OK);
    }


    @DeleteMapping("/forms/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<APIResponse<Object>> deleteFormTemplateByTemplateId(@PathVariable UUID id){

        formService.deleteFormTemplateById(id);

        APIResponse<Object> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form Template deleted successfully"
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }


    @DeleteMapping("/forms/{id}/data")
    public ResponseEntity<APIResponse<Object>> deleteFormDataById(@PathVariable UUID id){

        formService.deleteFormDataById(id);

        APIResponse<Object> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form Data deleted successfully"
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }


    @PutMapping("/forms/data/{id}")
    public ResponseEntity<APIResponse<FormDataResponse>> editFormById(@PathVariable UUID id, @RequestBody FormDataRequest newFormData){
        FormDataResponse formDataResponse= formService.editFormDataById(id,newFormData);

        APIResponse<FormDataResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form data edited successfully",
                formDataResponse
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }

    @GetMapping("/forms/data/{id}")
    public ResponseEntity<APIResponse<FormDataResponse>> getFormDataById(@PathVariable UUID id){
        FormDataResponse formDataResponse = formService.getFormDataById(id);

        APIResponse<FormDataResponse> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Form data retrieved successfully",
                formDataResponse
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);

    }

    @GetMapping("/forms/{id}/data/excel-sheet")
    public ResponseEntity<APIResponse<Object>> exportExcelSheet(@PathVariable UUID id , HttpServletResponse response){

        formService.exportFormDataToExcel(id,response);

        APIResponse<Object> apiResponse = new APIResponse<>(
                HttpStatus.OK,
                "Excel file exported successfully"
        );

        return new ResponseEntity<>(apiResponse,HttpStatus.OK);
    }


}
