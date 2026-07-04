package com.restaurante.resturante.controller.venta;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import com.restaurante.resturante.dto.venta.ReportesDto.SalesSummaryDto;
import com.restaurante.resturante.service.venta.IReporteService;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class ReporteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private IReporteService reporteService;

    @Test
    @WithMockUser(authorities = "read:admin-reports")
    public void getSalesSummary_WithAdminAuthority_ShouldAllowAccess() throws Exception {
        SalesSummaryDto dummySummary = new SalesSummaryDto(
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, Collections.emptyList()
        );
        when(reporteService.getSalesSummary(any(), any(), any())).thenReturn(dummySummary);

        mockMvc.perform(get("/api/reportes/sales")
                .param("sucursalIds", "suc-01")
                .param("fechaInicio", "2026-07-01")
                .param("fechaFin", "2026-07-02"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "read:different-authority")
    public void getSalesSummary_WithWrongAuthority_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/reportes/sales")
                .param("sucursalIds", "suc-01"))
                .andExpect(status().isForbidden());
    }

    @Test
    public void getSalesSummary_WithoutAuthentication_ShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(get("/api/reportes/sales"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(authorities = "export:admin-reports")
    public void exportPdf_WithExportAuthority_ShouldAllowAccess() throws Exception {
        when(reporteService.exportToPdf(any(), any(), any(), any())).thenReturn(new byte[0]);

        mockMvc.perform(get("/api/reportes/export/pdf")
                .param("reportType", "sales"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(authorities = "read:admin-reports")
    public void exportPdf_WithReadAuthorityOnly_ShouldReturnForbidden() throws Exception {
        mockMvc.perform(get("/api/reportes/export/pdf")
                .param("reportType", "sales"))
                .andExpect(status().isForbidden());
    }
}
