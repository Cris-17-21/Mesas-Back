package com.restaurante.resturante.controller.venta;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.AbrirCajaDto;
import com.restaurante.resturante.dto.venta.CajaResumentDto;
import com.restaurante.resturante.dto.venta.CajaTurnoDto;
import com.restaurante.resturante.dto.venta.CerrarCajaDto;
import com.restaurante.resturante.service.venta.ICajaTurnoService;
import com.restaurante.resturante.service.venta.jpa.CajaReporteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ventas/caja")
@RequiredArgsConstructor
public class CajaTurnoController {

    private final ICajaTurnoService cajaService;
    private final CajaReporteService reporteService;

    // Para saber si el cajero debe ver el formulario de apertura o el panel
    @GetMapping("/activa/{sucursalId}/{usuarioId}")
    public ResponseEntity<CajaTurnoDto> obtenerCajaActiva(
            @PathVariable String sucursalId,
            @PathVariable String usuarioId) {
        return cajaService.obtenerCajaActiva(sucursalId, usuarioId)
                .map(ResponseEntity::ok) // Si existe, devuelve 200 + DTO
                .orElse(ResponseEntity.noContent().build()); // Si no existe, devuelve 204 (o .ok(null))
    }

    @PostMapping("/abrir")
    public ResponseEntity<CajaTurnoDto> abrir(@RequestBody AbrirCajaDto dto) {
        // PERMISO: ABRIR_CAJA
        return ResponseEntity.ok(cajaService.abrirCaja(dto));
    }

    @GetMapping("/arqueo/{cajaId}")
    public ResponseEntity<CajaResumentDto> verArqueo(@PathVariable String cajaId) {
        // PERMISO: CONSULTAR_ARQUEO
        return ResponseEntity.ok(cajaService.obtenerResumenArqueo(cajaId));
    }

    @PostMapping("/cerrar")
    public ResponseEntity<Void> cerrar(@RequestBody CerrarCajaDto dto) {
        // PERMISO: CERRAR_CAJA
        cajaService.cerrarCaja(dto);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/historial/{sucursalId}")
    public ResponseEntity<List<CajaTurnoDto>> obtenerHistorial(@PathVariable String sucursalId) {
        return ResponseEntity.ok(cajaService.obtenerHistorial(sucursalId));
    }

    @GetMapping("/{cajaId}/reporte")
    public ResponseEntity<byte[]> descargarReporte(@PathVariable String cajaId) {
        byte[] pdf = reporteService.generarReportePDF(cajaId);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "caja-cierre-" + cajaId + ".pdf");
        return ResponseEntity.ok().headers(headers).body(pdf);
    }
}
