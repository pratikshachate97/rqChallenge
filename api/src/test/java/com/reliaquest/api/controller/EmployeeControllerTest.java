package com.reliaquest.api.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.reliaquest.api.dto.EmployeeDTO;
import com.reliaquest.api.exception.EmployeeNotFoundException;
import com.reliaquest.api.service.EmployeeService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmployeeController.class)
public class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    @Test
    void getAllEmployees_shouldReturnOkAndEmployeeList() throws Exception {
        List<EmployeeDTO> employees = new ArrayList<>();
        employees.add(EmployeeDTO.builder()
                .id("e143a192-5970-452f-87cb-027012838e78")
                .name("Lloyd Graham")
                .age(58)
                .salary(116571)
                .email("stim@company.com")
                .build());

        when(employeeService.getAllEmployees()).thenReturn(employees);

        mockMvc.perform(MockMvcRequestBuilders.get("/employees"))
                .andExpect(status().isOk());
    }

    @Test
    void getEmployeeById_shouldReturnOkAndEmployee() throws Exception {
        EmployeeDTO employee = EmployeeDTO.builder()
                .id("e143a192-5970-452f-87cb-027012838e78")
                .name("Lloyd Graham")
                .age(58)
                .salary(116571)
                .email("stim@company.com")
                .build();
        when(employeeService.getById("e143a192-5970-452f-87cb-027012838e78")).thenReturn(employee);

        mockMvc.perform(MockMvcRequestBuilders.get("/employees/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getEmployeeById_shouldReturnNotFoundWhenEmployeeNotFound() throws Exception {
        when(employeeService.getById("nonexistent")).thenThrow(new EmployeeNotFoundException("Employee with ID 'nonexistent' not found"));

        mockMvc.perform(MockMvcRequestBuilders.get("/employees/nonexistent"))
                .andExpect(status().isNotFound());
    }

    @Test
    void createEmployee_shouldReturnCreatedAndEmployee() throws Exception {
        Map<String, Object> employeeInput = Map.of("name", "New Employee", "age", 28, "salary", 55000, "email", "new.employee@example.com");
        EmployeeDTO createdEmployee = EmployeeDTO.builder()
                .id("e143a192-5970-452f-87cb-027012838e78")
                .name("Lloyd Graham")
                .age(58)
                .salary(116571)
                .email("stim@company.com")
                .build();
        when(employeeService.createEmployee(any())).thenReturn(createdEmployee);

        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(employeeInput)))
                .andExpect(status().isCreated());
    }

    @Test
    void createEmployee_shouldReturnBadRequestForInvalidInput() throws Exception {
        Map<String, Object> invalidInput = Map.of("name", "", "age", "abc");
        when(employeeService.createEmployee(any())).thenThrow(new IllegalArgumentException("Invalid input data"));

        mockMvc.perform(MockMvcRequestBuilders.post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidInput)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void deleteEmployeeById_shouldReturnOk() throws Exception {
        when(employeeService.deleteEmployeeById("1")).thenReturn("Employee with ID 1 deleted successfully.");

        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/1"))
                .andExpect(status().isOk())
                .andExpect(MockMvcResultMatchers.content().string("Employee with ID 1 deleted successfully."));
    }

    @Test
    void deleteEmployeeById_shouldReturnNotFoundWhenEmployeeNotFound() throws Exception {
        doThrow(new EmployeeNotFoundException("Employee with ID '123' not found"))
                .when(employeeService).deleteEmployeeById("123");

        mockMvc.perform(MockMvcRequestBuilders.delete("/employees/123"))
                .andExpect(status().isNotFound());
    }
}