package com.latin.dream.erp.application.dto;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
public record TarifaRequest(@NotBlank String modeloId,@NotNull @DecimalMin("0.00") @Digits(integer=10,fraction=2) BigDecimal precioBase,
    @NotNull @DecimalMin("0.00") @DecimalMax("100.00") @Digits(integer=3,fraction=2) BigDecimal porcentaje,
    @NotNull LocalDate vigenteDesde,LocalDate vigenteHasta) {}
