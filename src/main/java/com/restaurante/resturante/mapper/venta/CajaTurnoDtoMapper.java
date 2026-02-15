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
        if (entity == null)
            return null;

        return new CajaTurnoDto(
                entity.getId(),
                entity.getEstado(),
                entity.getFechaApertura(),
                entity.getFechaCierre(),
                entity.getUser().getUsername(),
                entity.getMontoApertura(),
                entity.getDiferencia() != null ? entity.getDiferencia() : BigDecimal.ZERO);
    }

    /**
     * Convierte la entidad a un DTO de Resumen (Dashboard del cajero).
     * Sigue el orden: id, montoApertura, ventasEfectivo, ventasTarjeta,
     * ventasOtros, totalEsperado, fechaApertura.
     */
    public CajaResumentDto toResumenDto(CajaTurno entity, BigDecimal ventasEfectivo, BigDecimal ventasTarjeta,
            BigDecimal ingresos, BigDecimal egresos) {
        BigDecimal totalVentas = ventasEfectivo.add(ventasTarjeta);

        // Total Esperado = Apertura + Ventas + Ingresos - Egresos
        BigDecimal totalEsperado = entity.getMontoApertura()
                .add(totalVentas)
                .add(ingresos)
                .subtract(egresos);

        // Usamos montoCierreReal que es el que definiste en la entidad
        BigDecimal cierreReal = entity.getMontoCierreReal() != null ? entity.getMontoCierreReal() : BigDecimal.ZERO;
        BigDecimal diferencia = cierreReal.subtract(totalEsperado);

        return new CajaResumentDto(
                // Metadatos
                entity.getId(),
                entity.getEstado(),
                entity.getFechaApertura(),
                entity.getFechaCierre(),
                entity.getUser().getUsername(),

                // Bloque 1: Flujo
                entity.getMontoApertura(),
                ingresos, // Ingresos caja chica
                egresos, // Egresos caja chica

                // Bloque 2: Ventas
                ventasEfectivo,
                ventasTarjeta,
                BigDecimal.ZERO, // Otros
                totalVentas, // Global

                // Bloque 3: Arqueo
                totalEsperado,
                cierreReal,
                diferencia);
    }
}
