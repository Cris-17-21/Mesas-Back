package com.restaurante.resturante.dto.auxiliar;

import com.fasterxml.jackson.annotation.JsonProperty;

public record PersonaDto(
    @JsonProperty("first_name") String nombres,
    @JsonProperty("first_last_name") String apellidoPaterno,
    @JsonProperty("second_last_name") String apellidoMaterno,
    @JsonProperty("document_number") String numeroDocumento,
    @JsonProperty("full_name") String nombreCompleto
) {}
