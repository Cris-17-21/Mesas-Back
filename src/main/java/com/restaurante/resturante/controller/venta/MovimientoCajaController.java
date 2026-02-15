package com.restaurante.resturante.controller.venta;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.restaurante.resturante.dto.venta.MovimientoCajaDto;
import com.restaurante.resturante.dto.venta.MovimientoCajaResponseDto;
import com.restaurante.resturante.service.venta.jpa.MovimientoCajaService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/movimientos-caja")
@RequiredArgsConstructor
public class MovimientoCajaController {

    private final MovimientoCajaService service;

    @PostMapping
    public ResponseEntity<MovimientoCajaResponseDto> registrarMovimiento(@RequestBody MovimientoCajaDto dto) {
        return ResponseEntity.ok(service.registrarMovimiento(dto));
    }

    @GetMapping("/caja/{cajaId}")
    public ResponseEntity<List<MovimientoCajaResponseDto>> listarPorCaja(@PathVariable String cajaId) {
        return ResponseEntity.ok(service.listarMovimientosPorCaja(cajaId));
    }
}
