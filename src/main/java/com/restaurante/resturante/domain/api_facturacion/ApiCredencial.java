package com.restaurante.resturante.domain.api_facturacion;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "api_credenciales")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ApiCredencial {
    @Id
    private Long id = 1L; // Solo un registro para el SuperAdmin

    @Column(length = 1000) // Los tokens suelen ser largos
    private String accessToken;

    @Column(length = 1000)
    private String refreshToken;

    private LocalDateTime expiryDate;

    @Column(name = "api_company_id", length = 36)
    private String apiCompanyId;

    // Getters, Setters y Helper para expiración
    public boolean needsRefresh() {
        return LocalDateTime.now().isAfter(this.expiryDate.minusMinutes(5));
    }
}
