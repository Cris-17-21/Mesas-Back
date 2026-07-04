package com.restaurante.resturante.service.venta;

import com.restaurante.resturante.dto.venta.ReportesDto.SalesSummaryDto;
import com.restaurante.resturante.dto.venta.ReportesDto.MenuRankingDto;
import com.restaurante.resturante.dto.venta.ReportesDto.CajaAuditDto;
import com.restaurante.resturante.dto.venta.ReportesDto.StockCriticalDto;
import com.restaurante.resturante.dto.venta.ReportesDto.WaiterPerformanceDto;

import java.time.LocalDate;
import java.util.List;

public interface IReporteService {

    SalesSummaryDto getSalesSummary(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin);

    List<MenuRankingDto> getMenuRanking(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin);

    List<CajaAuditDto> getCajaAudit(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin);

    StockCriticalDto getStockCritical(List<String> sucursalIds);

    List<WaiterPerformanceDto> getWaiterPerformance(List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin);

    byte[] exportToPdf(String reportType, List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin);

    byte[] exportToExcel(String reportType, List<String> sucursalIds, LocalDate fechaInicio, LocalDate fechaFin);
}
