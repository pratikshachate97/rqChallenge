package com.reliaquest.api.controller;

import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.service.EmployeeService;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
@Slf4j
public class EmployeeController implements IEmployeeController<EmployeeDTO, Map<String, Object>> {

    private final EmployeeService employeeService;

    @GetMapping
    public ResponseEntity<List<EmployeeDTO>> getAllEmployees() {
        try {
            return ResponseEntity.ok(employeeService.getAllEmployees());
        } catch (WebClientException e) {
            log.error("Error communicating with Mock API for all employees", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Failed to communicate with employee service", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching all employees", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch employees", e);
        }
    }

    @GetMapping("/search")
    public ResponseEntity<List<EmployeeDTO>> getEmployeesByNameSearch(@RequestParam String searchString) {
        try {
            return ResponseEntity.ok(employeeService.searchByName(searchString));
        } catch (WebClientException e) {
            log.error("Error communicating with Mock API for name search: {}", searchString, e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Failed to communicate with employee service", e);
        } catch (Exception e) {
            log.error("Unexpected error while searching employees by name: {}", searchString, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to search employees", e);
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<EmployeeDTO> getEmployeeById(@PathVariable String id) {
        try {
            EmployeeDTO employee = employeeService.getById(id);
            return ResponseEntity.ok(employee);
        } catch (EmployeeNotFoundException e) {
            log.warn("Employee not found with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Employee not found from Mock API with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found", e);
        } catch (WebClientException e) {
            log.error("Error communicating with Mock API to fetch employee by ID: {}", id, e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Failed to communicate with employee service", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching employee by ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch employee", e);
        }
    }

    @GetMapping("/highestSalary")
    public ResponseEntity<Integer> getHighestSalaryOfEmployees() {
        try {
            return ResponseEntity.ok(employeeService.getHighestSalary());
        } catch (WebClientException e) {
            log.error("Error communicating with Mock API for highest salary", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Failed to communicate with employee service", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching highest salary", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch highest salary", e);
        }
    }

    @GetMapping("/topTenHighestEarningEmployeeNames")
    public ResponseEntity<List<String>> getTopTenHighestEarningEmployeeNames() {
        try {
            return ResponseEntity.ok(employeeService.getTop10HighestEarners());
        } catch (WebClientException e) {
            log.error("Error communicating with Mock API for top earners", e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Failed to communicate with employee service", e);
        } catch (Exception e) {
            log.error("Unexpected error while fetching top ten highest earners", e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to fetch top earners", e);
        }
    }

    @Override
    public ResponseEntity<EmployeeDTO> createEmployee(@RequestBody Map<String, Object> employeeInput) {
        try {
            EmployeeDTO createdEmployee = employeeService.createEmployee(employeeInput);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdEmployee);
        } catch (IllegalArgumentException e) {
            log.warn("Invalid input for creating employee: {}", employeeInput, e);
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
        } catch (WebClientException e) {
            log.error("Error communicating with Mock API for creating employee: {}", employeeInput, e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Failed to communicate with employee service", e);
        } catch (Exception e) {
            log.error("Error while creating employee: {}", employeeInput, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to create employee", e);
        }
    }

    @Override
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteEmployeeById(@PathVariable String id) {
        try {
            String deletionResult = employeeService.deleteEmployeeById(id);
            return ResponseEntity.ok(deletionResult);
        } catch (EmployeeNotFoundException e) {
            log.warn("Employee not found for deletion with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, e.getMessage(), e);
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Employee not found from Mock API for deletion with ID: {}", id);
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found", e);
        } catch (WebClientException e) {
            log.error("Error communicating with Mock API for deleting employee with ID: {}", id, e);
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE, "Failed to communicate with employee service", e);
        } catch (Exception e) {
            log.error("Unexpected error while deleting employee with ID: {}", id, e);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Failed to delete employee", e);
        }
    }
}
