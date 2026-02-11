package com.restaurante.resturante.mapper.venta;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.restaurante.resturante.domain.ventas.CajaTurno;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;

@Component
public class CajaTurnoDtoMapper {

    /**
     * Convierte la entidad a un DTO básico (Listados o confirmación de apertura).
     * Sigue el orden: id, montoApertura, fechaApertura, estado, nombreUsuario.
     */
    public CajaTurnoDto toDto(CajaTurno entity) {
        if (entity == null) return null;

        return new CajaTurnoDto(
            entity.getId(),
            entity.getMontoApertura(),
            entity.getFechaApertura(),
            entity.getEstado(),
            (entity.getUser() != null) ? entity.getUser().getNombres() : "N/A"
        );
    }

    /**
     * Convierte la entidad a un DTO de Resumen (Dashboard del cajero).
     * Sigue el orden: id, montoApertura, ventasEfectivo, ventasTarjeta, ventasOtros, totalEsperado, fechaApertura.
     */
    public CajaResumentDto toResumenDto(CajaTurno entity,
                                        BigDecimal efectivo,
                                        BigDecimal tarjetas,
                                        BigDecimal otros) {
        if (entity == null) return null;

        // Calculamos el total esperado: Apertura + ventas en efectivo
        BigDecimal totalEsperado = entity.getMontoApertura().add(efectivo);

        return new CajaResumentDto(
            entity.getId(),                // 1. id
            entity.getMontoApertura(),     // 2. montoApertura
            efectivo,                      // 3. totalVentasEfectivo
            tarjetas,                      // 4. totalVentasTarjeta
            otros,                         // 5. totalOtrosMedios
            totalEsperado,                 // 6. totalEsperado
            entity.getFechaApertura()      // 7. fechaApertura
        );
    }
}
