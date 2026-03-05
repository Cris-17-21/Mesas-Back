package com.restaurante.resturante.controller.venta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
import com.restaurante.resturante.dto.venta.NotaCreditoRequestDto;
import com.restaurante.resturante.service.venta.jpa.FacturacionComprobanteService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

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
}
