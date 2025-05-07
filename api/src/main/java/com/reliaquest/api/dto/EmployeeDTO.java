package com.reliaquest.api.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeDTO {

    private String id;

    @NotBlank(message = "Name is required")
    @JsonProperty("employee_name")
    private String name;

    @Min(value = 1, message = "Salary must be greater than zero")
    @JsonProperty("employee_salary")
    private int salary;

    @Min(value = 16, message = "Age must be at least 16")
    @Max(value = 75, message = "Age must be at most 75")
    @JsonProperty("employee_age")
    private int age;

    @NotBlank(message = "Title is required")
    @JsonProperty("employee_title")
    private String title;

    @Email(message = "Email should be valid")
    @JsonProperty("employee_email")
    private String email;
}
