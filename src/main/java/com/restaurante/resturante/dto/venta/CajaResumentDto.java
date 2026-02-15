package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record CajaResumentDto(
        // --- Metadatos ---
        String id,
        String estado,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        String usuarioNombre,

        // --- Bloque 1: Flujo de Dinero ---
        BigDecimal montoInicial, // Base
        BigDecimal totalIngresosCajaChica, // Entradas manuales (no ventas)
        BigDecimal totalEgresosCajaChica, // Gastos

        // --- Bloque 2: Ventas (Calculadas desde Pedidos) ---
        BigDecimal totalVentasEfectivo,
        BigDecimal totalVentasTarjeta,
        BigDecimal totalVentasOtros, // Yape, Plin, Transferencia
        BigDecimal totalVentasGlobal, // La suma de todo lo vendido

        // --- Bloque 3: Arqueo (La prueba de fuego) ---
        BigDecimal saldoEsperadoEnCaja, // (Inicial + VentasEfectivo + Ingresos) - Egresos
        BigDecimal saldoRealEnCaja, // Lo que contó el cajero al cerrar
        BigDecimal diferencia) {
    public BigDecimal totalEsperado() {
        return saldoEsperadoEnCaja;
    }
}