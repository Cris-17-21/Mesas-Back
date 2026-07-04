package com.restaurante.resturante.dto.venta;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public final class ReportesDto {

    private ReportesDto() {}

    public record SalesSummaryDto(
        BigDecimal totalVentas,
        BigDecimal promedioTicket,
        BigDecimal totalEfectivo,
        BigDecimal totalTarjeta,
        BigDecimal totalYape,
        BigDecimal totalPlin,
        BigDecimal totalOtros,
        List<VentaDiariaDto> ventasDiarias
    ) {}

    public record VentaDiariaDto(
        LocalDate fecha,
        BigDecimal total,
        Long cantidadPedidos
    ) {}

    public record MenuRankingDto(
        String nombrePlato,
        Long cantidad,
        BigDecimal ingresos
    ) {}

    public record CajaAuditDto(
        String cajaTurnoId,
        String sucursalNombre,
        String usuarioNombre,
        LocalDateTime fechaApertura,
        LocalDateTime fechaCierre,
        BigDecimal montoInicial,
        BigDecimal ingresosCajaChica,
        BigDecimal egresosCajaChica,
        BigDecimal saldoEsperado,
        BigDecimal saldoReal,
        BigDecimal diferencia,
        String estado
    ) {}

    public record StockCriticalDto(
        BigDecimal valoracionTotalInventario,
        List<ProductoBajoStockDto> productosBajoStock
    ) {}

    public record ProductoBajoStockDto(
        Integer productoId,
        String nombreProducto,
        String sucursalNombre,
        BigDecimal stockActual,
        BigDecimal stockMinimo,
        String unidadMedida
    ) {}

    public record WaiterPerformanceDto(
        String waiterNombre,
        Long cantidadPedidos,
        BigDecimal totalVendido
    ) {}
}
