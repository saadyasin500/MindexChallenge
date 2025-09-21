package com.mindex.challenge.controller.requests;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CompensationRequest {
  @NotNull(message = "Salary is required") // for extensibility, to have the API return a custom error message, I would use a RestControllerAdvice class but decided to keep it simple for this small project
  @Positive(message = "Salary should be greater than zero")
  private BigDecimal salary;
  private LocalDate effectiveDate;
}
