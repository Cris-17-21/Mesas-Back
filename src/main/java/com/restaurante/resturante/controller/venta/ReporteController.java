package com.restaurante.resturante.controller.venta;

import com.restaurante.resturante.dto.venta.ReportesDto.CajaAuditDto;
import com.restaurante.resturante.dto.venta.ReportesDto.MenuRankingDto;
import com.restaurante.resturante.dto.venta.ReportesDto.SalesSummaryDto;
import com.restaurante.resturante.dto.venta.ReportesDto.StockCriticalDto;
import com.restaurante.resturante.dto.venta.ReportesDto.WaiterPerformanceDto;
import com.restaurante.resturante.service.venta.IReporteService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reportes")
@RequiredArgsConstructor
public class ReporteController {

    private final IReporteService reporteService;

    @GetMapping("/sales")
    @PreAuthorize("hasAuthority('read:admin-reports')")
    public ResponseEntity<SalesSummaryDto> getSalesSummary(
            @RequestParam(value = "sucursalIds", required = false) List<String> sucursalIds,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.getSalesSummary(sucursalIds, fechaInicio, fechaFin));
    }

    @GetMapping("/menu-ranking")
    @PreAuthorize("hasAuthority('read:admin-reports')")
    public ResponseEntity<List<MenuRankingDto>> getMenuRanking(
            @RequestParam(value = "sucursalIds", required = false) List<String> sucursalIds,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.getMenuRanking(sucursalIds, fechaInicio, fechaFin));
    }

    @GetMapping("/caja-audit")
    @PreAuthorize("hasAuthority('read:admin-reports')")
    public ResponseEntity<List<CajaAuditDto>> getCajaAudit(
            @RequestParam(value = "sucursalIds", required = false) List<String> sucursalIds,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.getCajaAudit(sucursalIds, fechaInicio, fechaFin));
    }

    @GetMapping("/stock-critical")
    @PreAuthorize("hasAuthority('read:admin-reports')")
    public ResponseEntity<StockCriticalDto> getStockCritical(
            @RequestParam(value = "sucursalIds", required = false) List<String> sucursalIds) {
        return ResponseEntity.ok(reporteService.getStockCritical(sucursalIds));
    }

    @GetMapping("/waiter-performance")
    @PreAuthorize("hasAuthority('read:admin-reports')")
    public ResponseEntity<List<WaiterPerformanceDto>> getWaiterPerformance(
            @RequestParam(value = "sucursalIds", required = false) List<String> sucursalIds,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return ResponseEntity.ok(reporteService.getWaiterPerformance(sucursalIds, fechaInicio, fechaFin));
    }

    @GetMapping("/export/pdf")
    @PreAuthorize("hasAuthority('export:admin-reports')")
    public ResponseEntity<byte[]> exportPdf(
            @RequestParam("reportType") String reportType,
            @RequestParam(value = "sucursalIds", required = false) List<String> sucursalIds,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        byte[] pdfBytes = reporteService.exportToPdf(reportType, sucursalIds, fechaInicio, fechaFin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte_" + reportType.toLowerCase() + ".pdf")
                .build());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(pdfBytes);
    }

    @GetMapping("/export/excel")
    @PreAuthorize("hasAuthority('export:admin-reports')")
    public ResponseEntity<byte[]> exportExcel(
            @RequestParam("reportType") String reportType,
            @RequestParam(value = "sucursalIds", required = false) List<String> sucursalIds,
            @RequestParam(value = "fechaInicio", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(value = "fechaFin", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        
        byte[] excelBytes = reporteService.exportToExcel(reportType, sucursalIds, fechaInicio, fechaFin);
        
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDisposition(ContentDisposition.attachment()
                .filename("reporte_" + reportType.toLowerCase() + ".xlsx")
                .build());
        
        return ResponseEntity.ok()
                .headers(headers)
                .body(excelBytes);
    }
}
