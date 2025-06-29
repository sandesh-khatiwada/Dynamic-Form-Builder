package com.sandesh.formbuilder.controller;

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



}
