package com.example.payroll_management_system.config;

import com.example.payroll_management_system.dto.responseDto.ErrorDto;
import com.example.payroll_management_system.exceptions.InvalidCallException;
import com.example.payroll_management_system.exceptions.OwnershipException;
import com.example.payroll_management_system.exceptions.ResourceNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    Logger log = LoggerFactory.getLogger("GlobalExceptionHandler.class");

    // id is not there in DB (employee id, manager id etc)
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorDto> handleResourceNotFoundException(
            ResourceNotFoundException e
    ){
        log.warn("Id not found in DB: {}", e.getMessage());

        //always reply the object not the string
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Id not found in DB",
                                Instant.now())
                );
    }

    // @Valid failed in the dto (blank email, null hours etc)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,String>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e
    ){
        Map<String,String> map = new HashMap<>();
        BindingResult bindingResult =  e.getBindingResult();
        List<FieldError> list = bindingResult.getFieldErrors();
        list.forEach(fieldError -> {
                    map.put(fieldError.getField(), fieldError.getDefaultMessage());
                    log.warn("Field {} , failed validation: {}", fieldError.getField(), fieldError.getDefaultMessage());
                }
        );

        return ResponseEntity
                .badRequest()
                .body(map );
    }

    // data is valid json but not correct as per our rules (hours > 24, wrong dates etc)
    @ExceptionHandler(InvalidCallException.class)
    public ResponseEntity<ErrorDto> handleInvalidCallException(
            InvalidCallException e
    ){
        log.warn("Invalid call: {}", e.getMessage());

        //always reply the object not the string
        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Call is Invalid due to malformed data",
                                Instant.now())
                );
    }

    // the logged in user is touching data of someone else
    @ExceptionHandler(OwnershipException.class)
    public ResponseEntity<ErrorDto> handleOwnershipException(
            OwnershipException e
    ){
        log.warn("Ownership check failed: {}", e.getMessage());

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto(e.getMessage(),
                                "Logged-in User cannot perform this operation",
                                Instant.now())
                );
    }

    // duplicate email or employee code (unique columns in DB)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorDto> handleDataIntegrityViolationException(
            DataIntegrityViolationException e
    ){
        log.warn("Duplicate or invalid data");

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(
                        new ErrorDto("Duplicate or invalid data",
                                "Email or employee code already exists",
                                Instant.now())
                );
    }

    // bad json, wrong date format (01-10-2026) or wrong enum value (type: TAX)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorDto> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e
    ){
        log.warn("Malformed request body");

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto("Malformed request body",
                                "Check the json format, dates (yyyy-MM-dd) and enum values",
                                Instant.now())
                );
    }

    // wrong value in the url or request param, like status=BOSS or id=abc
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorDto> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e
    ){
        log.warn("Invalid value for {}", e.getName());

        return ResponseEntity
                .badRequest()
                .body(
                        new ErrorDto("Invalid value for '" + e.getName() + "'",
                                "Check the allowed values for this field",
                                Instant.now())
                );
    }
}
