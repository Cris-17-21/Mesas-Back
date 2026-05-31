package com.restaurante.resturante.controller.venta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
import com.restaurante.resturante.dto.venta.NotaCreditoRequestDto;
import com.restaurante.resturante.service.venta.jpa.FacturacionComprobanteService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/facturacion")
@RequiredArgsConstructor
public class FacturacionController {

    private final FacturacionComprobanteService service;

    @PostMapping("/emitir")
    public ResponseEntity<FacturacionComprobanteDto> emitirComprobante(@RequestBody FacturaRequestDto dto) {
        return ResponseEntity.ok(service.emitirComprobante(dto));
    }

    @GetMapping("/sucursal/{sucursalId}")
    public ResponseEntity<java.util.List<FacturacionComprobanteDto>> listarPorSucursal(
            @PathVariable String sucursalId) {
        return ResponseEntity.ok(service.listarComprobantes(sucursalId));
    }

    @PostMapping("/nota-credito")
    public ResponseEntity<FacturacionComprobanteDto> emitirNotaCredito(@RequestBody NotaCreditoRequestDto dto) {
        return ResponseEntity.ok(service.emitirNotaCredito(dto));
    }

    @GetMapping("/buscar")
    public ResponseEntity<java.util.List<FacturacionComprobanteDto>> buscarComprobantes(
            @RequestParam String sucursalId,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String serie,
            @RequestParam(required = false) String correlativo,
            @RequestParam(required = false) String fechaInicio,
            @RequestParam(required = false) String fechaFin) {
        return ResponseEntity.ok(service.buscarComprobantesConFiltros(
                sucursalId, tipo, serie, correlativo, fechaInicio, fechaFin));
    }

    @PostMapping("/comprobantes/{id}/enviar-inmediato")
    public ResponseEntity<FacturacionComprobanteDto> enviarInmediato(@PathVariable String id) {
        return ResponseEntity.ok(service.enviarComprobanteManual(id));
    }

    @DeleteMapping("/comprobantes/{id}")
    public ResponseEntity<Void> eliminarPendiente(@PathVariable String id) {
        service.eliminarComprobantePendiente(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/comprobantes/{id}/descargar/{tipo}")
    public ResponseEntity<byte[]> descargarArchivo(@PathVariable String id, @PathVariable String tipo) {
        byte[] archivo = service.obtenerArchivoComprobante(id, tipo);
        String contentType = tipo.equalsIgnoreCase("pdf") ? "application/pdf" : "application/xml";
        String extension = tipo.equalsIgnoreCase("pdf") ? ".pdf" : ".xml";
        
        return ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, contentType)
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"comprobante-" + id + extension + "\"")
                .body(archivo);
    }

    @PostMapping("/series/configurar")
    public ResponseEntity<Void> configurarSeries(@RequestParam String sucursalId, @RequestParam String tipoDoc, @RequestParam String serie, @RequestParam Integer correlativo) {
        service.configurarSerieCorrelativo(sucursalId, tipoDoc, serie, correlativo);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/series")
    public ResponseEntity<java.util.List<java.util.Map<String, Object>>> obtenerSeries(@RequestParam String sucursalId) {
        return ResponseEntity.ok(service.obtenerSeriesPorSucursal(sucursalId));
    }
}
