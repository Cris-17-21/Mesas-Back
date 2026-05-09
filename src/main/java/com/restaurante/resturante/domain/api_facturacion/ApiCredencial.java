package com.restaurante.resturante.domain.api_facturacion;

import java.time.LocalDateTime;

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
    private Long id; // Siempre 1 para el acceso maestro
    private String accessToken;
    private String refreshToken;
    private LocalDateTime expiryDate; // Calculado: ahora + 1 hora

    // Método helper para saber si expiró
    public boolean isExpired() {
        return LocalDateTime.now().isAfter(this.expiryDate.minusMinutes(5)); // Margen de seguridad de 5 min
    }
}
