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
                entity.getCodigoApertura(),
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
    public CajaResumentDto toResumenDto(CajaTurno entity, BigDecimal ventasEfectivo, BigDecimal ventasVirtual,
            BigDecimal ingresosEfectivo, BigDecimal egresosEfectivo,
            BigDecimal ingresosVirtual, BigDecimal egresosVirtual) {
        BigDecimal totalVentas = ventasEfectivo.add(ventasVirtual);

        BigDecimal cashApertura = entity.getMontoAperturaEfectivo() != null ? entity.getMontoAperturaEfectivo() : entity.getMontoApertura();
        BigDecimal totalEsperadoEfectivo = cashApertura
                .add(ventasEfectivo)
                .add(ingresosEfectivo)
                .subtract(egresosEfectivo);

        BigDecimal cardApertura = entity.getMontoAperturaVirtual() != null ? entity.getMontoAperturaVirtual() : BigDecimal.ZERO;
        BigDecimal totalEsperadoVirtual = cardApertura
                .add(ventasVirtual)
                .add(ingresosVirtual)
                .subtract(egresosVirtual);

        BigDecimal cashCierreReal = entity.getMontoCierreRealEfectivo() != null ? entity.getMontoCierreRealEfectivo() : (entity.getMontoCierreReal() != null ? entity.getMontoCierreReal() : BigDecimal.ZERO);
        BigDecimal cardCierreReal = entity.getMontoCierreRealVirtual() != null ? entity.getMontoCierreRealVirtual() : BigDecimal.ZERO;
        BigDecimal diferencia = entity.getDiferencia() != null ? entity.getDiferencia() : (cashCierreReal.add(cardCierreReal)).subtract(totalEsperadoEfectivo.add(totalEsperadoVirtual));

        return new CajaResumentDto(
                // Metadatos
                entity.getId(),
                entity.getCodigoApertura(),
                entity.getEstado(),
                entity.getFechaApertura(),
                entity.getFechaCierre(),
                entity.getUser().getUsername(),

                // Bloque 1: Flujo (Caja Chica Total)
                entity.getMontoApertura(),
                ingresosEfectivo.add(ingresosVirtual), // Ingresos caja chica
                egresosEfectivo.add(egresosVirtual), // Egresos caja chica

                // Bloque 2: Ventas
                ventasEfectivo,
                ventasVirtual,
                BigDecimal.ZERO, // Otros
                totalVentas, // Global

                // Bloque 3: Arqueo
                totalEsperadoEfectivo,
                totalEsperadoVirtual,
                cashCierreReal,
                diferencia,

                // Bloque 4: Desglose Apertura / Cierre
                cashApertura,
                cardApertura,
                cashCierreReal,
                cardCierreReal);
    }
}
