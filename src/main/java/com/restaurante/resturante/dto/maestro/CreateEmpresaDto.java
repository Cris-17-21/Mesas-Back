package com.restaurante.resturante.dto.maestro;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateEmpresaDto(
        @NotBlank(message = "El RUC es obligatorio.") @Size(min = 11, max = 11, message = "El RUC debe tener 11 dígitos.") String ruc,
        @NotBlank(message = "La razón social es obligatoria.") String razonSocial,
        String direccionFiscal,
        @Pattern(regexp = "^[0-9]{9}$", message = "El teléfono debe tener 9 dígitos.") String telefono,
        @Email(message = "El email debe ser válido.") String email,
        String logoUrl,
        String fechaAfiliacion) {
}
