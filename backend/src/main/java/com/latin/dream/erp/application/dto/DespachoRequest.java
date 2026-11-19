package com.latin.dream.erp.application.dto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
public record DespachoRequest(
    @NotBlank @Pattern(regexp="[0-9a-fA-F-]{36}") String solicitudId,
    @NotBlank @Size(max=150) String cliente, @NotBlank @Size(max=200) String destino,
    @NotBlank @Size(max=50) String vehiculo, @NotBlank @Size(max=100) String conductor,
    @NotNull LocalDate fechaSalida, @NotNull LocalDate fechaEntregaEstimada,
    @NotEmpty @Size(max=100) List<@Valid Asignacion> lotes
) {
    public record Asignacion(@NotBlank String loteId,@NotNull @Min(1) Integer cantidad) {}
}
