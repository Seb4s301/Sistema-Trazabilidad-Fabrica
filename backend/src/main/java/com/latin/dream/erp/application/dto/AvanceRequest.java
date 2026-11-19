package com.latin.dream.erp.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record AvanceRequest(
    @JsonProperty("lote_id") @NotBlank String loteId,
    @JsonProperty("area_destino") @NotBlank String areaDestino,
    @Size(max = 20) String resultado,
    @Size(max = 1000) String notas
) {}
