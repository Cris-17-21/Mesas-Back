package com.restaurante.resturante.controller.venta;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.FacturaRequestDto;
import com.restaurante.resturante.dto.venta.FacturacionComprobanteDto;
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
}
