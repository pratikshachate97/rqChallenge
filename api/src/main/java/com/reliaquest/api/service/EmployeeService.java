package com.reliaquest.api.service;

import com.reliaquest.api.dto.ApiResponseWrapper;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.exception.EmployeeNotFoundException;

import java.util.*;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmployeeService {

    @Autowired
    public WebClient webClient;

    public List<EmployeeDTO> getAllEmployees() {
        try {
            return webClient
                    .get()
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<List<EmployeeDTO>>>() {
                    })
                    .map(ApiResponseWrapper::getData)
                    .block();
        } catch (WebClientException e) {
            log.error("Error fetching all employees from Mock API");
            throw new RuntimeException("Failed to fetch employees from employee service", e);
        }
    }

    public List<EmployeeDTO> searchByName(String fragment) {
        return getAllEmployees().stream()
                .filter(e -> e.getName().toLowerCase().contains(fragment.toLowerCase()))
                .collect(Collectors.toList());
    }

    public EmployeeDTO getById(String id) {
        log.info("Fetching employee with ID: {}", id);
        try {
            return webClient
                    .get()
                    .uri("/{id}", id)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<EmployeeDTO>>() {
                    })
                    .map(ApiResponseWrapper::getData)
                    .block();
        } catch (WebClientResponseException.NotFound e) {
            log.warn("Employee not found from Mock API with ID: {}", id);
            throw new EmployeeNotFoundException("Employee with ID '" + id + "' not found");
        } catch (WebClientException e) {
            log.error("Error fetching employee by ID {} from Mock API", id);
            throw new RuntimeException("Failed to fetch employee from employee service", e);
        }
    }

    public int getHighestSalary() {
        List<EmployeeDTO> employees = getAllEmployees();
        return employees.stream()
                .mapToInt(EmployeeDTO::getSalary)
                .max()
                .orElseThrow(() -> new NoSuchElementException("No employees found to determine highest salary"));
    }

    public List<String> getTop10HighestEarners() {
        return getAllEmployees().stream()
                .sorted(Comparator.comparingInt(EmployeeDTO::getSalary).reversed())
                .limit(10)
                .map(EmployeeDTO::getName)
                .collect(Collectors.toList());
    }

    public EmployeeDTO createEmployee(Map<String, Object> employeeInput) {
        try {
            ApiResponseWrapper<EmployeeDTO> response = webClient
                    .post()
                    .uri("")
                    .bodyValue(employeeInput)
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponseWrapper<EmployeeDTO>>() {
                    })
                    .block();
            return response != null ? response.getData() : null;
        } catch (WebClientException e) {
            log.error("Error creating employee via Mock API");
            throw new RuntimeException("Failed to create employee via employee service", e);
        }
    }

    public String deleteEmployeeById(String id) {
        log.info("Deleting employee with ID: {}", id);
        EmployeeDTO employeeToDelete;
        try {
            employeeToDelete = getById(id);
        } catch (EmployeeNotFoundException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error retrieving employee for deletion with ID: {}", id);
            throw new RuntimeException("Error retrieving employee for deletion", e);
        }

        if (employeeToDelete == null || employeeToDelete.getName() == null) {
            throw new EmployeeNotFoundException("Employee with ID " + id + " not found or name is missing.");
        }

        try {
            Mono<ResponseEntity<ApiResponseWrapper<Boolean>>> responseEntityMono = webClient
                    .method(HttpMethod.DELETE)
                    .uri("")
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(Map.of("name", employeeToDelete.getName()))
                    .retrieve()
                    .toEntity(new ParameterizedTypeReference<>() {
                    });

            ResponseEntity<ApiResponseWrapper<Boolean>> responseEntity = responseEntityMono.block();

            if (responseEntity != null
                    && responseEntity.getStatusCode().is2xxSuccessful()
                    && responseEntity.getBody() != null
                    && Boolean.TRUE.equals(responseEntity.getBody().getData())) {
                return "Employee with ID " + id + " (name: " + employeeToDelete.getName() + ") deleted successfully.";
            } else if (responseEntity != null
                    && responseEntity.getBody() != null
                    && responseEntity.getBody().getStatus() != null) {
                throw new RuntimeException(
                        "Failed to delete employee: " + responseEntity.getBody().getStatus());
            } else if (responseEntity != null && !responseEntity.getStatusCode().is2xxSuccessful()) {
                throw new RuntimeException("Failed to delete employee. Status Code: "
                        + responseEntity.getStatusCode());
            } else {
                throw new RuntimeException(
                        "Failed to delete employee due to an unexpected response from employee service.");
            }
        } catch (WebClientException e) {
            log.error("Error deleting employee with ID {} via Mock API", id);
            throw new RuntimeException("Failed to delete employee via employee service", e);
        }
    }
}
