package com.restaurante.resturante.dto.auxiliar;

import com.fasterxml.jackson.annotation.JsonProperty;

public record EmpresaDto(
    @JsonProperty("razon_social") String razonSocial,
    @JsonProperty("numero_documento") String numeroDocumento,
    @JsonProperty("estado") String estado,
    @JsonProperty("condicion") String condicion,
    @JsonProperty("direccion") String direccion
) {}
